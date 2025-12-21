package com.example.commitech.data.api

import com.example.commitech.data.model.AuthResponse
import com.example.commitech.data.model.LoginRequest
import com.example.commitech.data.model.RegisterRequest
import com.example.commitech.data.model.PendaftarListResponse
import com.example.commitech.data.model.PendaftarSingleResponse
import com.example.commitech.data.model.PendaftarResponse
import com.example.commitech.data.model.ImportExcelResponse
import com.example.commitech.data.model.UpdateStatusSeleksiBerkasRequest
import com.example.commitech.data.model.HasilWawancaraRequest
import com.example.commitech.data.model.HasilWawancaraSingleResponse
import com.example.commitech.data.model.SessionValidationResponse
import com.example.commitech.data.model.ActiveSessionsResponse
import com.example.commitech.data.model.JadwalRekrutmenResponse
import com.example.commitech.data.model.JadwalRekrutmenSingleResponse
import com.example.commitech.data.model.JadwalRekrutmenItem
import com.example.commitech.data.model.AssignPesertaRequest
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
    // API Session Management
    // ==========================================
    
    @GET("api/session/check")
    suspend fun checkSession(@Header("Authorization") token: String): Response<SessionValidationResponse>
    
    @GET("api/session/list")
    suspend fun getActiveSessions(@Header("Authorization") token: String): Response<ActiveSessionsResponse>
    
    @DELETE("api/session/{id}")
    suspend fun revokeSession(
        @Header("Authorization") token: String,
        @Path("id") sessionId: String
    ): Response<Unit>
    
    @POST("api/session/revoke-others")
    suspend fun revokeOtherSessions(@Header("Authorization") token: String): Response<Unit>
    
    // ==========================================
    // API Peserta/Data Pendaftar
    // ==========================================
    
    @GET("api/peserta")
    suspend fun getPesertaList(
        @Header("Authorization") token: String,
        @retrofit2.http.Query("page") page: Int = 1,
        @retrofit2.http.Query("per_page") perPage: Int = 20
    ): Response<PendaftarListResponse>
    
    @GET("api/peserta/{id}")
    suspend fun getPesertaById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<PendaftarSingleResponse>
    
    @GET("api/peserta/lulus-tanpa-jadwal")
    suspend fun getPesertaLulusTanpaJadwal(
        @Header("Authorization") token: String
    ): Response<PendaftarListResponse>
    
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
    
    @PUT("api/peserta/{id}/status-seleksi-berkas")
    suspend fun updateStatusSeleksiBerkas(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateStatusSeleksiBerkasRequest
    ): Response<PendaftarSingleResponse>
    
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
    // API Jadwal Rekrutmen
    // ==========================================
    
    @GET("api/jadwal-rekrutmen")
    suspend fun getJadwalRekrutmen(
        @Header("Authorization") token: String
    ): Response<JadwalRekrutmenResponse>
    
    @POST("api/jadwal-rekrutmen")
    suspend fun createJadwalRekrutmen(
        @Header("Authorization") token: String,
        @Body jadwal: JadwalRekrutmenItem
    ): Response<JadwalRekrutmenSingleResponse>
    
    @PUT("api/jadwal-rekrutmen/{id}")
    suspend fun updateJadwalRekrutmen(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body jadwal: JadwalRekrutmenItem
    ): Response<JadwalRekrutmenSingleResponse>
    
    @DELETE("api/jadwal-rekrutmen/{id}")
    suspend fun deleteJadwalRekrutmen(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
    
    @POST("api/jadwal-rekrutmen/{id}/peserta")
    suspend fun assignPesertaToJadwal(
        @Header("Authorization") token: String,
        @Path("id") jadwalId: Int,
        @Body request: AssignPesertaRequest
    ): Response<JadwalRekrutmenSingleResponse>
    
    @GET("api/jadwal-rekrutmen/{id}/peserta")
    suspend fun getPesertaByJadwal(
        @Header("Authorization") token: String,
        @Path("id") jadwalId: Int
    ): Response<PendaftarListResponse>
    
    @DELETE("api/jadwal-rekrutmen/{id}/peserta/{pesertaId}")
    suspend fun removePesertaFromJadwal(
        @Header("Authorization") token: String,
        @Path("id") jadwalId: Int,
        @Path("pesertaId") pesertaId: Int
    ): Response<Unit>
}
