package com.example.commitech.data.api

import com.example.commitech.data.model.AuthResponse
import com.example.commitech.data.model.LoginRequest
import com.example.commitech.data.model.RegisterRequest
import com.example.commitech.data.model.PendaftarListResponse
import com.example.commitech.data.model.PendaftarSingleResponse
import com.example.commitech.data.model.PendaftarResponse
import com.example.commitech.data.model.ImportExcelResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("api/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>
    
    @GET("api/user")
    suspend fun getUser(@Header("Authorization") token: String): Response<AuthResponse>
    
    // ==========================================
    // API Peserta/Data Pendaftar
    // ==========================================
    
    @GET("api/peserta")
    suspend fun getPesertaList(
        @Header("Authorization") token: String
    ): Response<PendaftarListResponse>
    
    @GET("api/peserta/{id}")
    suspend fun getPesertaById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<PendaftarSingleResponse>
    
    @POST("api/peserta")
    suspend fun createPeserta(
        @Header("Authorization") token: String,
        @Body pendaftar: PendaftarResponse
    ): Response<PendaftarSingleResponse>
    
    @PUT("api/peserta/{id}")
    suspend fun updatePeserta(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body pendaftar: PendaftarResponse
    ): Response<PendaftarSingleResponse>
    
    @DELETE("api/peserta/{id}")
    suspend fun deletePeserta(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
    
    @Multipart
    @POST("api/peserta/import-excel")
    suspend fun importExcel(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<ImportExcelResponse>
}
