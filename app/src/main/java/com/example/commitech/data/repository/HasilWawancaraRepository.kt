package com.example.commitech.data.repository

import com.example.commitech.data.api.RetrofitClient
import com.example.commitech.data.model.HasilWawancaraRequest
import com.example.commitech.data.model.HasilWawancaraSingleResponse
import com.example.commitech.data.model.HasilWawancaraListResponse
import retrofit2.Response

class HasilWawancaraRepository {
    
    private val apiService = RetrofitClient.apiService

    suspend fun simpanHasilWawancara(
        token: String,
        request: HasilWawancaraRequest
    ): Response<HasilWawancaraSingleResponse> {
        return apiService.simpanHasilWawancara("Bearer $token", request)
    }

    suspend fun getHasilWawancara(
        token: String
    ): Response<HasilWawancaraListResponse> {
        return apiService.getHasilWawancara("Bearer $token")
    }

    suspend fun ubahHasilWawancara(
        token: String,
        id: Int,
        request: HasilWawancaraRequest
    ): Response<HasilWawancaraSingleResponse> {
        return apiService.ubahHasilWawancara("Bearer $token", id, request)
    }
}

