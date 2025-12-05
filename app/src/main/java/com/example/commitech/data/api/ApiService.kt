package com.example.commitech.data.api

import com.example.commitech.data.model.AuthResponse
import com.example.commitech.data.model.LoginRequest
import com.example.commitech.data.model.RegisterRequest
import com.example.commitech.data.model.PendaftarListResponse
import com.example.commitech.data.model.PendaftarSingleResponse
import com.example.commitech.data.model.PendaftarResponse
import com.example.commitech.data.model.ImportExcelResponse
import com.example.commitech.data.model.HasilWawancaraRequest
import com.example.commitech.data.model.HasilWawancaraSingleResponse
import com.example.commitech.data.model.SessionValidationResponse
import com.example.commitech.data.model.ActiveSessionsResponse
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
        @Header("Authorization") token: String,
        @retrofit2.http.Query("page") page: Int = 1,
        @retrofit2.http.Query("per_page") perPage: Int = 20,
        @retrofit2.http.Query("search") search: String? = null
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
    
    // ==========================================
    // API Hasil Wawancara (Modul 4 - Fitur 16-17)
    // ==========================================
    
    /**
     * Fitur 16: Input hasil wawancara (Create)
     * 
     * Endpoint: POST /api/wawancara/hasil
     * 
     * Menyimpan hasil wawancara untuk seorang peserta.
     * 
     * @param token Token autentikasi (dengan prefix "Bearer")
     * @param request Request body berisi data hasil wawancara
     * @return Response yang berisi data hasil wawancara yang baru dibuat
     * 
     * Status Code:
     * - 201: Created - Hasil wawancara berhasil disimpan
     * - 400: Bad Request - Hasil wawancara untuk peserta ini sudah ada
     * - 422: Unprocessable Entity - Validation error
     * - 401: Unauthorized - Token invalid/expired
     */
    @POST("api/wawancara/hasil")
    suspend fun simpanHasilWawancara(
        @Header("Authorization") token: String,
        @Body request: HasilWawancaraRequest
    ): Response<HasilWawancaraSingleResponse>
    
    // ==========================================
    // API Session Management (HYBRID APPROACH)
    // ==========================================
    
    /**
     * Check session validity
     * 
     * CRITICAL: Core dari hybrid session management
     * - Validate token di server
     * - Check expiry time
     * - Return user data jika valid
     * 
     * Endpoint: GET /api/session/check
     * 
     * @param token Session token (dengan prefix "Bearer")
     * @return SessionValidationResponse
     * 
     * Response:
     * {
     *   "isValid": true,
     *   "user": { ... },
     *   "expiresIn": 25  // minutes remaining
     * }
     */
    @GET("api/session/check")
    suspend fun checkSession(
        @Header("Authorization") token: String
    ): Response<SessionValidationResponse>
    
    /**
     * Get list of active sessions
     * 
     * USE CASE: Settings → Active Sessions
     * User bisa lihat semua device yang sedang login
     * 
     * Endpoint: GET /api/session/list
     * 
     * @param token Session token (dengan prefix "Bearer")
     * @return ActiveSessionsResponse dengan list sessions
     */
    @GET("api/session/list")
    suspend fun getActiveSessions(
        @Header("Authorization") token: String
    ): Response<ActiveSessionsResponse>
    
    /**
     * Revoke specific session by ID
     * 
     * USE CASE: Logout dari device tertentu
     * 
     * Endpoint: DELETE /api/session/{id}
     * 
     * @param token Current session token
     * @param sessionId Session ID to revoke (String, bukan Int)
     */
    @DELETE("api/session/{id}")
    suspend fun revokeSession(
        @Header("Authorization") token: String,
        @Path("id") sessionId: String
    ): Response<Unit>
    
    /**
     * Revoke all other sessions except current
     * 
     * USE CASE: "Logout from all other devices" button
     * 
     * Endpoint: POST /api/session/revoke-others
     * 
     * @param token Current session token
     */
    @POST("api/session/revoke-others")
    suspend fun revokeOtherSessions(
        @Header("Authorization") token: String
    ): Response<Unit>
}
