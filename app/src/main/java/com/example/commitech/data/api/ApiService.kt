package com.example.commitech.data.api

import com.example.commitech.data.model.AuthResponse
import com.example.commitech.data.model.LoginRequest
import com.example.commitech.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("api/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>
    
    @GET("api/user")
    suspend fun getUser(@Header("Authorization") token: String): Response<AuthResponse>
}
