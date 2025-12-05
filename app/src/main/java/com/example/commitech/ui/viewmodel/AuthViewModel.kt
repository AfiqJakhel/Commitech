package com.example.commitech.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.commitech.data.model.ErrorResponse
import com.example.commitech.data.model.SessionInfo
import com.example.commitech.data.model.User
import com.example.commitech.data.repository.AuthRepository
import com.example.commitech.utils.DeviceInfoHelper
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import android.os.Build
import android.provider.Settings

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val token: String? = null,
    val error: String? = null
)

/**
 * AuthViewModel - ViewModel untuk mengelola autentikasi user
 *
 * INSTAGRAM-STYLE SESSION MANAGEMENT:
 * - Session persist di SharedPreferences ✅
 * - Multi-device support ✅
 * - Session expire after 7 days (from login time) ✅
 * - Server-side validation ✅
 *
 * BEHAVIOR:
 * - App killed/restart → Masih login ✅
 * - Phone restart → Masih login ✅
 * - Tidak buka 7 hari → Auto logout ✅
 * - Manual logout → Logout ✅
 * - Multi-device tracking ✅
 *
 * CRITICAL: Token PERSIST di SharedPreferences!
 * Session expire based on time (7 days), bukan activity.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val gson = Gson()

    // ============================================================================
    // INSTAGRAM-STYLE SESSION MANAGEMENT
    // ============================================================================

    private val sharedPreferences = application.getSharedPreferences(
        "commitech_auth",
        Context.MODE_PRIVATE
    )

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Active sessions untuk multi-device tracking
    private val _activeSessions = MutableStateFlow<List<SessionInfo>>(emptyList())
    val activeSessions: StateFlow<List<SessionInfo>> = _activeSessions.asStateFlow()

    private val _isLoadingSessions = MutableStateFlow(false)
    val isLoadingSessions: StateFlow<Boolean> = _isLoadingSessions.asStateFlow()

    // Session expiry: 7 hari
    private val SESSION_EXPIRY_DAYS = 7

    init {
        // CRITICAL: Load auth state dari SharedPreferences
        // Ini membuat user tetap login meskipun app killed
        loadAuthState()
    }

    /**
     * Load auth state dari SharedPreferences
     *
     * INSTAGRAM-STYLE:
     * - Load token & user data
     * - Check local expiry (7 days)
     * - Validate dengan server
     * - Auto logout jika expired
     */
    private fun loadAuthState() {
        viewModelScope.launch {
            val token = sharedPreferences.getString("auth_token", null)
            val loginAt = sharedPreferences.getLong("login_at", 0)
            
            // DEBUG: Log loaded data
            android.util.Log.d("AuthViewModel", "=== LOADING AUTH STATE ===")
            android.util.Log.d("AuthViewModel", "Token: ${token?.take(20) ?: "NULL"}")
            android.util.Log.d("AuthViewModel", "Login At: $loginAt")
            android.util.Log.d("AuthViewModel", "==========================")

            if (token != null && loginAt > 0) {
                // Check local expiry
                val now = System.currentTimeMillis()
                val daysSinceLogin = (now - loginAt) / (24 * 60 * 60 * 1000L)

                if (daysSinceLogin >= SESSION_EXPIRY_DAYS) {
                    // Session expired locally (> 7 days)
                    clearAuthState()
                    _authState.value = AuthState(
                        isLoading = false,
                        error = "Session expired. Please login again."
                    )
                    return@launch
                }

                // Session masih valid locally, validate dengan server
                _authState.value = _authState.value.copy(isLoading = true)

                try {
                    val response = repository.checkSession(token)

                    if (response.isSuccessful && response.body()?.isValid == true) {
                        // Session valid di server
                        val validation = response.body()!!

                        _authState.value = AuthState(
                            isLoading = false,
                            isAuthenticated = true,
                            user = validation.user,
                            token = token,
                            error = null
                        )
                    } else {
                        // Session invalid di server
                        clearAuthState()
                        _authState.value = AuthState(
                            isLoading = false,
                            error = "Session expired. Please login again."
                        )
                    }
                } catch (e: Exception) {
                    // Network error - Allow offline usage
                    // Load cached user data
                    val userName = sharedPreferences.getString("user_name", null)
                    val userEmail = sharedPreferences.getString("user_email", null)
                    val userId = sharedPreferences.getInt("user_id", 0)

                    if (userName != null && userEmail != null && userId > 0) {
                        _authState.value = AuthState(
                            isLoading = false,
                            isAuthenticated = true,
                            user = User(
                                id = userId,
                                name = userName,
                                email = userEmail
                            ),
                            token = token,
                            error = null
                        )
                    } else {
                        clearAuthState()
                        _authState.value = AuthState(
                            isLoading = false,
                            error = "Cannot validate session. Please check your connection."
                        )
                    }
                }
            } else {
                // No token, user harus login
                android.util.Log.d("AuthViewModel", " No token or loginAt = 0, user must login")
                _authState.value = AuthState(isLoading = false)
            }
        }
    }

    /**
     * Save auth state ke SharedPreferences
     *
     * INSTAGRAM-STYLE:
     * - Save token, user data, dan login timestamp
     * - Login timestamp untuk check local expiry
     * 
     * CRITICAL: Use commit() instead of apply()
     * - commit() = synchronous, langsung save ke disk
     * - apply() = async, mungkin belum selesai saat app killed
     */
    private fun saveAuthState(token: String, user: User) {
        val success = sharedPreferences.edit().apply {
            putString("auth_token", token)
            putInt("user_id", user.id)
            putString("user_name", user.name)
            putString("user_email", user.email)
            putLong("login_at", System.currentTimeMillis())
        }.commit() // CRITICAL: Use commit() for immediate save
        
        // DEBUG: Log saved data
        android.util.Log.d("AuthViewModel", "=== SAVED AUTH STATE ===")
        android.util.Log.d("AuthViewModel", "Success: $success")
        android.util.Log.d("AuthViewModel", "Token: ${token.take(20)}...")
        android.util.Log.d("AuthViewModel", "User ID: ${user.id}")
        android.util.Log.d("AuthViewModel", "User Name: ${user.name}")
        android.util.Log.d("AuthViewModel", "Login At: ${System.currentTimeMillis()}")
        android.util.Log.d("AuthViewModel", "========================")
    }

    /**
     * Clear auth state dari SharedPreferences
     */
    private fun clearAuthState() {
        sharedPreferences.edit().clear().commit() // Use commit()
        android.util.Log.d("AuthViewModel", " AUTH STATE CLEARED")
    }

    /**
     * Parse error response from server
     */
    private fun parseErrorResponse(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) {
            return "Terjadi kesalahan pada server"
        }

        return try {
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message ?: "Terjadi kesalahan pada server"
        } catch (e: JsonSyntaxException) {
            "Terjadi kesalahan: Format response tidak valid"
        }
    }

    /**
     * Login function dengan device info
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            try {
<<<<<<< HEAD
                val appContext = getApplication<Application>().applicationContext
                val deviceName = Build.MODEL ?: "Unknown Device"
                val deviceType = "android"
                val deviceId = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
                    ?: "unknown-device-id"
                
                val response = repository.login(email, password, deviceName, deviceType, deviceId)
                
=======
                // Collect device info
                val deviceName = DeviceInfoHelper.getDeviceName()
                val deviceType = DeviceInfoHelper.getDeviceType()
                val deviceId = DeviceInfoHelper.getDeviceId(getApplication())

                // Login dengan device info
                val response = repository.login(
                    email = email,
                    password = password,
                    deviceName = deviceName,
                    deviceType = deviceType,
                    deviceId = deviceId
                )

>>>>>>> a0b982a03be01fd7f5e42552fc1794d46136a22f
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    val token = authResponse.data.token
                    val user = authResponse.data.user

                    // CRITICAL: Save auth state ke SharedPreferences
                    // Agar user tetap login meskipun app killed
                    saveAuthState(token, user)

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = user,
                        token = token,
                        error = null
                    )
                } else {
                    val errorBody = try {
                        response.errorBody()?.string()
                    } catch (e: IOException) {
                        null
                    }

                    val errorMessage = parseErrorResponse(errorBody) ?: "Login gagal. Status: ${response.code()}"

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            } catch (e: JsonSyntaxException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: Format response tidak valid"
                )
            } catch (e: IOException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: Tidak dapat terhubung ke server. Pastikan server berjalan."
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    /**
     * Register function dengan device info
     */
    fun register(name: String, email: String, password: String, passwordConfirmation: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            try {
                // Collect device info
                val deviceName = DeviceInfoHelper.getDeviceName()
                val deviceType = DeviceInfoHelper.getDeviceType()
                val deviceId = DeviceInfoHelper.getDeviceId(getApplication())

                // Register dengan device info
                val response = repository.register(
                    name,
                    email,
                    password,
                    passwordConfirmation,
                    deviceName,
                    deviceType,
                    deviceId
                )

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    val token = authResponse.data.token
                    val user = authResponse.data.user

                    // CRITICAL: Save auth state ke SharedPreferences
                    saveAuthState(token, user)

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = user,
                        token = token,
                        error = null
                    )
                } else{
                    val errorBody = try {
                        response.errorBody()?.string()
                    } catch (e: IOException) {
                        null
                    }

                    val errorMessage = parseErrorResponse(errorBody) ?: "Registrasi gagal. Status: ${response.code()}"

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            } catch (e: JsonSyntaxException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: Format response tidak valid"
                )
            } catch (e: IOException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: Tidak dapat terhubung ke server. Pastikan server berjalan."
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    /**
     * Logout function
     */
    fun logout() {
        val currentToken = _authState.value.token

        // Clear auth state dari memory
        _authState.value = AuthState()

        // CRITICAL: Clear auth state dari SharedPreferences
        clearAuthState()

        // Call API logout di background
        viewModelScope.launch {
            try {
                if (currentToken != null) {
                    repository.logout(currentToken)
                }
            } catch (e: Exception) {
                // Ignore error
            }
        }
    }

    /**
     * Load active sessions untuk multi-device tracking
     */
    fun loadActiveSessions() {
        viewModelScope.launch {
            _isLoadingSessions.value = true

            try {
                val token = _authState.value.token ?: return@launch
                val response = repository.getActiveSessions(token)

                if (response.isSuccessful && response.body() != null) {
                    _activeSessions.value = response.body()!!.sessions
                }
            } catch (e: Exception) {
                // Ignore error
            } finally {
                _isLoadingSessions.value = false
            }
        }
    }

    /**
     * Revoke specific session
     */
    fun revokeSession(sessionId: String) {
        viewModelScope.launch {
            try {
                val token = _authState.value.token ?: return@launch
                val response = repository.revokeSession(token, sessionId)

                if (response.isSuccessful) {
                    // Reload sessions
                    loadActiveSessions()
                }
            } catch (e: Exception) {
                // Ignore error
            }
        }
    }

    /**
     * Revoke all other sessions
     */
    fun revokeOtherSessions() {
        viewModelScope.launch {
            try {
                val token = _authState.value.token ?: return@launch
                val response = repository.revokeOtherSessions(token)

                if (response.isSuccessful) {
                    // Reload sessions
                    loadActiveSessions()
                }
            } catch (e: Exception) {
                // Ignore error
            }
        }
    }

    /**
     * Get days until session expiry
     */
    fun getDaysUntilExpiry(): Int {
        val loginAt = sharedPreferences.getLong("login_at", 0)
        if (loginAt == 0L) return 0

        val now = System.currentTimeMillis()
        val daysSinceLogin = (now - loginAt) / (24 * 60 * 60 * 1000L)
        val daysRemaining = SESSION_EXPIRY_DAYS - daysSinceLogin.toInt()

        return maxOf(0, daysRemaining)
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}