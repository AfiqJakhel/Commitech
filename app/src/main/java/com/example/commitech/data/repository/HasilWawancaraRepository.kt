package com.example.commitech.data.repository

import com.example.commitech.data.api.RetrofitClient
import com.example.commitech.data.model.HasilWawancaraRequest
import com.example.commitech.data.model.HasilWawancaraSingleResponse
import retrofit2.Response

/**
 * Repository untuk handle operasi API terkait Hasil Wawancara
 * 
 * Fitur: Modul 4 - Fitur 16: Input Hasil Wawancara (Create)
 * 
 * Repository ini bertanggung jawab untuk:
 * - Memanggil API endpoint hasil wawancara
 * - Handle request/response ke backend
 * - Menyediakan layer abstraksi antara ViewModel dan API service
 * 
 * Usage di ViewModel:
 * ```
 * val repository = HasilWawancaraRepository()
 * val response = repository.simpanHasilWawancara(token, request)
 * ```
 */
class HasilWawancaraRepository {
    
    private val apiService = RetrofitClient.apiService
    
    /**
     * Menyimpan hasil wawancara ke backend via API
     * 
     * @param token Token autentikasi dari AuthViewModel (tanpa prefix "Bearer")
     * @param request Request body yang berisi data hasil wawancara
     * @return Response dari backend yang berisi HasilWawancaraSingleResponse
     * 
     * Example:
     * ```
     * val request = HasilWawancaraRequest(
     *     pesertaId = 1,
     *     status = "diterima",
     *     divisi = "Acara",
     *     alasan = null
     * )
     * val response = repository.simpanHasilWawancara("your_token_here", request)
     * ```
     * 
     * Error Handling:
     * - 401: Unauthorized (token expired/invalid) - perlu re-login
     * - 400: Bad Request (sudah ada hasil wawancara untuk peserta ini)
     * - 422: Validation Error (field tidak valid)
     * - 500: Server Error
     */
    suspend fun simpanHasilWawancara(
        token: String,
        request: HasilWawancaraRequest
    ): Response<HasilWawancaraSingleResponse> {
        // Token sudah termasuk prefix "Bearer" di ApiService
        return apiService.simpanHasilWawancara("Bearer $token", request)
    }
}

