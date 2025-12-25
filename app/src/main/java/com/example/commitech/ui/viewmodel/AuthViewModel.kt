package com.example.commitech.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.commitech.data.model.ErrorResponse
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
import androidx.core.content.edit

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val token: String? = null,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val gson = Gson()

    private val sharedPreferences = application.getSharedPreferences(
        "commitech_auth",
        Context.MODE_PRIVATE
    )

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val SESSION_EXPIRY_DAYS = 7

    init {
        loadAuthState()
    }

    private fun loadAuthState() {
        viewModelScope.launch {
            val token = sharedPreferences.getString("auth_token", null)
            val loginAt = sharedPreferences.getLong("login_at", 0)

            android.util.Log.d("AuthViewModel", "=== LOADING AUTH STATE ===")
            android.util.Log.d("AuthViewModel", "Token: ${token?.take(20) ?: "NULL"}")
            android.util.Log.d("AuthViewModel", "Login At: $loginAt")
            android.util.Log.d("AuthViewModel", "==========================")

            if (token != null && loginAt > 0) {
                val now = System.currentTimeMillis()
                val daysSinceLogin = (now - loginAt) / (24 * 60 * 60 * 1000L)

                if (daysSinceLogin >= SESSION_EXPIRY_DAYS) {
                    clearAuthState()
                    _authState.value = AuthState(
                        isLoading = false,
                        error = "Session expired. Please login again."
                    )
                    return@launch
                }

                _authState.value = _authState.value.copy(isLoading = true)

                try {
                    val response = repository.checkSession(token)

                    if (response.isSuccessful && response.body()?.isValid == true) {
                        val validation = response.body()!!

                        _authState.value = AuthState(
                            isLoading = false,
                            isAuthenticated = true,
                            user = validation.user,
                            token = token,
                            error = null
                        )
                    } else {
                        clearAuthState()
                        _authState.value = AuthState(
                            isLoading = false,
                            error = "Session expired. Please login again."
                        )
                    }
                } catch (_: Exception) {
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
                android.util.Log.d("AuthViewModel", " No token or loginAt = 0, user must login")
                _authState.value = AuthState(isLoading = false)
            }
        }
    }

    private fun saveAuthState(token: String, user: User) {
        val success = sharedPreferences.edit().apply {
            putString("auth_token", token)
            putInt("user_id", user.id)
            putString("user_name", user.name)
            putString("user_email", user.email)
            putLong("login_at", System.currentTimeMillis())
        }.commit()

        android.util.Log.d("AuthViewModel", "=== SAVED AUTH STATE ===")
        android.util.Log.d("AuthViewModel", "Success: $success")
        android.util.Log.d("AuthViewModel", "Token: ${token.take(20)}...")
        android.util.Log.d("AuthViewModel", "User ID: ${user.id}")
        android.util.Log.d("AuthViewModel", "User Name: ${user.name}")
        android.util.Log.d("AuthViewModel", "Login At: ${System.currentTimeMillis()}")
        android.util.Log.d("AuthViewModel", "========================")
    }

    private fun clearAuthState() {
        sharedPreferences.edit { clear() }
        android.util.Log.d("AuthViewModel", " AUTH STATE CLEARED")
    }

    private fun parseErrorResponse(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) {
            return "Terjadi kesalahan pada server"
        }

        return try {
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message ?: "Terjadi kesalahan pada server"
        } catch (_: JsonSyntaxException) {
            "Terjadi kesalahan: Format response tidak valid"
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            try {
                val appContext = getApplication<Application>().applicationContext
                val deviceName = Build.MODEL ?: "Unknown Device"
                val deviceType = "android"
                val deviceId = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
                    ?: "unknown-device-id"
                
                val response = repository.login(email, password, deviceName, deviceType, deviceId)
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    val token = authResponse.data.token
                    val user = authResponse.data.user

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
                    } catch (_: IOException) {
                        null
                    }

                    val errorMessage = parseErrorResponse(errorBody) ?: "Login gagal. Status: ${response.code()}"

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            } catch (_: JsonSyntaxException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: Format response tidak valid"
                )
            } catch (_: IOException) {
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

    fun register(name: String, email: String, password: String, passwordConfirmation: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            try {
                val deviceName = DeviceInfoHelper.getDeviceName()
                val deviceType = DeviceInfoHelper.getDeviceType()
                val deviceId = DeviceInfoHelper.getDeviceId(getApplication())

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
                    } catch (_: IOException) {
                        null
                    }

                    val errorMessage = parseErrorResponse(errorBody) ?: "Registrasi gagal. Status: ${response.code()}"

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            } catch (_: JsonSyntaxException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: Format response tidak valid"
                )
            } catch (_: IOException) {
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

    fun logout() {
        val currentToken = _authState.value.token

        _authState.value = AuthState()

        clearAuthState()

        viewModelScope.launch {
            try {
                if (currentToken != null) {
                    repository.logout(currentToken)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}