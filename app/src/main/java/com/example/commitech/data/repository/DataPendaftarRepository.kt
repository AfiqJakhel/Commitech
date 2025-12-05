package com.example.commitech.data.repository

import com.example.commitech.data.api.RetrofitClient
import com.example.commitech.data.model.ImportExcelResponse
import com.example.commitech.data.model.PendaftarListResponse
import com.example.commitech.data.model.PendaftarResponse
import com.example.commitech.data.model.PendaftarSingleResponse
import com.example.commitech.data.model.UpdateStatusSeleksiBerkasRequest
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

class DataPendaftarRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun getPesertaList(
        token: String,
        page: Int = 1,
        perPage: Int = 20
    ): Response<PendaftarListResponse> {
        return apiService.getPesertaList("Bearer $token", page, perPage)
    }
    
    suspend fun getPesertaById(token: String, id: Int): Response<PendaftarSingleResponse> {
        return apiService.getPesertaById("Bearer $token", id)
    }
    
    suspend fun createPeserta(token: String, pendaftar: PendaftarResponse): Response<PendaftarSingleResponse> {
        return apiService.createPeserta("Bearer $token", pendaftar)
    }
    
    suspend fun updatePeserta(token: String, id: Int, pendaftar: PendaftarResponse): Response<PendaftarSingleResponse> {
        return apiService.updatePeserta("Bearer $token", id, pendaftar)
    }
    
    suspend fun deletePeserta(token: String, id: Int): Response<Unit> {
        return apiService.deletePeserta("Bearer $token", id)
    }
    
    suspend fun importExcel(token: String, file: File): Response<ImportExcelResponse> {
        val requestFile = file.asRequestBody("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        return apiService.importExcel("Bearer $token", body)
    }
    
    suspend fun updateStatusSeleksiBerkas(
        token: String,
        id: Int,
        request: UpdateStatusSeleksiBerkasRequest
    ): Response<PendaftarSingleResponse> {
        return apiService.updateStatusSeleksiBerkas("Bearer $token", id, request)
    }
}



