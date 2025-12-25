package com.example.commitech.data.repository

import com.example.commitech.data.api.RetrofitClient
import com.example.commitech.data.model.AuthResponse
import com.example.commitech.data.model.LoginRequest
import com.example.commitech.data.model.RegisterRequest
import com.example.commitech.data.model.SessionValidationResponse
import com.example.commitech.data.model.ActiveSessionsResponse
import retrofit2.Response

class AuthRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        deviceName: String,
        deviceType: String,
        deviceId: String
    ): Response<AuthResponse> {
        val request = RegisterRequest(
            name, 
            email, 
            password, 
            passwordConfirmation,
            deviceName,
            deviceType,
            deviceId
        )
        return apiService.register(request)
    }
    
    suspend fun login(
        email: String,
        password: String,
        deviceName: String,
        deviceType: String,
        deviceId: String
    ): Response<AuthResponse> {
        val request = LoginRequest(
            email = email,
            password = password,
            device_name = deviceName,
            device_type = deviceType,
            device_id = deviceId
        )
        return apiService.login(request)
    }
    
    suspend fun logout(token: String): Response<Unit> {
        return apiService.logout("Bearer $token")
    }


    suspend fun checkSession(token: String): Response<SessionValidationResponse> {
        return apiService.checkSession("Bearer $token")
    }

    suspend fun getActiveSessions(token: String): Response<ActiveSessionsResponse> {
        return apiService.getActiveSessions("Bearer $token")
    }

    suspend fun revokeSession(token: String, sessionId: String): Response<Unit> {
        return apiService.revokeSession("Bearer $token", sessionId)
    }

    suspend fun revokeOtherSessions(token: String): Response<Unit> {
        return apiService.revokeOtherSessions("Bearer $token")
    }
}
