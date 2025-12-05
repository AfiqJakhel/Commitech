package com.example.commitech.data.repository

import com.example.commitech.data.api.RetrofitClient
import com.example.commitech.data.model.AuthResponse
import com.example.commitech.data.model.LoginRequest
import com.example.commitech.data.model.RegisterRequest
import retrofit2.Response

class AuthRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String
    ): Response<AuthResponse> {
        val request = RegisterRequest(name, email, password, passwordConfirmation)
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
    
    suspend fun getUser(token: String): Response<AuthResponse> {
        return apiService.getUser("Bearer $token")
    }
}
