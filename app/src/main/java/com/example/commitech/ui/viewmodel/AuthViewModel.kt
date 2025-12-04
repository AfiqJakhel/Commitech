package com.example.commitech.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.commitech.data.model.ErrorResponse
import com.example.commitech.data.model.User
import com.example.commitech.data.repository.AuthRepository
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

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
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // Tidak auto-login, user harus login manual setiap kali
        _authState.value = AuthState(isLoading = false)
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
            val errorMessage = errorResponse.message ?: "Terjadi kesalahan"
            
            // Format validation errors nicely
            val validationErrors = errorResponse.errors?.flatMap { (field, messages) ->
                // Convert field name to readable format
                val fieldName = when (field) {
                    "email" -> "Email"
                    "password" -> "Password"
                    "name" -> "Nama"
                    "password_confirmation" -> "Konfirmasi Password"
                    else -> field.replace("_", " ").replaceFirstChar { it.uppercaseChar() }
                }
                messages.map { "• $fieldName: $it" }
            }?.joinToString("\n")
            
            if (!validationErrors.isNullOrBlank()) {
                if (errorMessage != "Terjadi kesalahan" && errorMessage != "Validation failed") {
                    "$errorMessage\n\n$validationErrors"
                } else {
                    validationErrors
                }
            } else {
                errorMessage
            }
        } catch (e: JsonSyntaxException) {
            // If JSON parsing fails, return raw error body (cleaned up)
            errorBody.trim().takeIf { it.isNotBlank() } ?: "Terjadi kesalahan pada server"
        } catch (e: Exception) {
            "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            try {
                val response = repository.login(email, password)
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    val token = authResponse.data.token

                    
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = authResponse.data.user,
                        token = token,
                        error = null
                    )
                } else {
                    // Get error message from server
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
    
    fun register(name: String, email: String, password: String, passwordConfirmation: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            try {
                val response = repository.register(name, email, password, passwordConfirmation)
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    val token = authResponse.data.token

                    
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = authResponse.data.user,
                        token = token,
                        error = null
                    )
                } else {
                    // Get error message from server
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
    
    fun logout() {
        val currentToken = _authState.value.token
        _authState.value = AuthState()
        
        // Panggil API logout di background
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
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
