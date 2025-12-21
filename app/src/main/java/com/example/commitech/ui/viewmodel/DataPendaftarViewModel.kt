package com.example.commitech.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.commitech.data.model.PendaftarResponse
import com.example.commitech.data.repository.DataPendaftarRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

// Data class untuk UI
data class Pendaftar(
    val id: Int,
    val nama: String,
    val nim: String,
    val divisi1: String,
    val alasan1: String,
    val divisi2: String,
    val alasan2: String,
    val krsTerakhir: String? = null, // Link Google Drive KRS
    val formulirPendaftaran: String? = null, // Link Google Drive Formulir
    val suratKomitmen: String? = null // Link Google Drive Surat Komitmen
)

// Helper function to convert PendaftarResponse to Pendaftar
fun PendaftarResponse.toPendaftar(): Pendaftar {
    return Pendaftar(
        id = id,
        nama = nama ?: "Nama Tidak Diketahui", // Fix: Handle null dengan default value
        nim = nim ?: "",
        divisi1 = pilihanDivisi1 ?: "",
        alasan1 = alasan1 ?: "",
        divisi2 = pilihanDivisi2 ?: "",
        alasan2 = alasan2 ?: "",
        krsTerakhir = krsTerakhir, // PERBAIKAN: Tambah link KRS
        formulirPendaftaran = if (formulirPendaftaran == true) "Sudah" else null, // Convert Boolean to String
        suratKomitmen = if (suratKomitmen == true) "Sudah" else null // Convert Boolean to String
    )
}

// Helper function to convert Pendaftar to PendaftarResponse
fun Pendaftar.toPendaftarResponse(): PendaftarResponse {
    return PendaftarResponse(
        id = id,
        nama = nama,
        nim = nim.takeIf { it.isNotEmpty() },
        email = null,
        telepon = null,
        jurusan = null,
        angkatan = null,
        pilihanDivisi1 = divisi1.takeIf { it.isNotEmpty() },
        pilihanDivisi2 = divisi2.takeIf { it.isNotEmpty() },
        pilihanDivisi3 = null,
        alasan1 = alasan1.takeIf { it.isNotEmpty() },
        alasan2 = alasan2.takeIf { it.isNotEmpty() },
        alasan3 = null,
        krsTerakhir = null,
        formulirPendaftaran = null,
        suratKomitmen = null,
        pindahDivisi = null,
        tanggalJadwal = null,
        waktuJadwal = null,
        lokasi = null,
        statusSeleksiBerkas = null
    )
}

data class DataPendaftarState(
    val isLoading: Boolean = false,
    val isReloading: Boolean = false, // Loading untuk reload setelah import
    val isLoadingMore: Boolean = false, // Loading untuk load more (pagination)
    val error: String? = null,
    val importMessage: String? = null,
    val importErrors: List<String>? = null, // PERBAIKAN: Tambah detail import errors
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasMore: Boolean = false,
    val searchQuery: String = "" // FITUR BARU: Search query
)

class DataPendaftarViewModel : ViewModel() {

    private val repository = DataPendaftarRepository()
    
    private val _pendaftarList = MutableStateFlow<List<Pendaftar>>(emptyList())
    val pendaftarList: StateFlow<List<Pendaftar>> = _pendaftarList.asStateFlow()
    
    private val _state = MutableStateFlow(DataPendaftarState())
    val state: StateFlow<DataPendaftarState> = _state.asStateFlow()

    fun loadPendaftarList(token: String?, page: Int = 1, append: Boolean = false, search: String? = null) {
        if (token == null) {
            _state.value = _state.value.copy(error = "Token tidak tersedia. Silakan login ulang.")
            return
        }
        
        viewModelScope.launch {
            // Jangan set loading jika ini reload setelah import (untuk avoid double loading)
            val isReloadAfterImport = _state.value.importMessage != null
            
            if (!isReloadAfterImport && !append) {
                _state.value = _state.value.copy(isLoading = true, error = null)
            } else if (append) {
                _state.value = _state.value.copy(isLoadingMore = true, error = null)
            }
            
            // Retry mechanism untuk handle intermittent connection issues
            // Response time backend ~510ms, jadi retry dengan delay pendek
            var success = false
            var lastError: String? = null
            val maxRetries = 3 // Coba maksimal 3 kali
            
            // Note: Search functionality akan dihandle di backend nanti
            // Untuk sekarang kita load semua data
            
            repeat(maxRetries) { attempt ->
                if (success) return@repeat
                
                try {
                    val response = repository.getPesertaList(token, page, 20)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val pendaftarResponses = body.data
                        val pagination = body.pagination
                        
                        // Update pagination info
                        val currentPage = pagination?.currentPage ?: page
                        val totalPages = pagination?.lastPage ?: 1
                        val totalItems = pagination?.total ?: 0
                        val hasMore = pagination?.hasMore ?: false
                        
                        // Append atau replace list
                        if (append) {
                            _pendaftarList.value = _pendaftarList.value + pendaftarResponses.map { it.toPendaftar() }
                        } else {
                            _pendaftarList.value = pendaftarResponses.map { it.toPendaftar() }
                        }
                        
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = null,
                            currentPage = currentPage,
                            totalPages = totalPages,
                            totalItems = totalItems,
                            hasMore = hasMore
                        )
                        success = true
                        return@repeat // Success, exit retry loop
                    } else {
                        val errorBody = try {
                            response.errorBody()?.string()
                        } catch (e: IOException) {
                            null
                        }
                        lastError = errorBody ?: "Gagal memuat data. Status: ${response.code()}"
                        
                        // Jika HTTP error (4xx/5xx), tidak perlu retry
                        // Hanya retry untuk network errors
                        if (response.code() in 400..599) {
                            success = false
                            return@repeat
                        }
                    }
                } catch (e: IOException) {
                    // Network error - bisa retry
                    lastError = "Tidak dapat terhubung ke server. Pastikan server berjalan."
                    
                    // Jika bukan attempt terakhir, tunggu sebentar sebelum retry
                    if (attempt < maxRetries - 1) {
                        // Exponential backoff: 500ms, 1000ms, 2000ms
                        val delayMs = (500 * (1 shl attempt)).coerceAtMost(2000)
                        delay(delayMs.toLong())
                    }
                } catch (e: Exception) {
                    // Unexpected error
                    lastError = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
                    
                    // Jika bukan attempt terakhir, tunggu sebentar sebelum retry
                    if (attempt < maxRetries - 1) {
                        val delayMs = (500 * (1 shl attempt)).coerceAtMost(2000)
                        delay(delayMs.toLong())
                    }
                }
            }
            
            // Jika semua retry gagal, tampilkan error
            if (!success && lastError != null) {
                // Jangan override import message jika ini reload setelah import
                if (isReloadAfterImport) {
                    // Silent fail - hanya log, jangan tampilkan error
                    Log.w("DataPendaftarViewModel", "Gagal reload list setelah import setelah $maxRetries attempts: $lastError")
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = lastError
                    )
                }
            }
        }
    }
    
    fun loadNextPage(token: String?) {
        val currentState = _state.value
        if (currentState.hasMore && !currentState.isLoadingMore && !currentState.isLoading) {
            loadPendaftarList(token, currentState.currentPage + 1, append = true)
        }
    }

    fun deletePendaftar(token: String?, pendaftar: Pendaftar) {
        if (token == null) {
            _state.value = _state.value.copy(error = "Token tidak tersedia. Silakan login ulang.")
            return
        }
        
        viewModelScope.launch {
            try {
                val response = repository.deletePeserta(token, pendaftar.id)
                
                if (response.isSuccessful) {
                    // Remove from local list
                    _pendaftarList.update { listSaatIni ->
                        listSaatIni.filterNot { it.id == pendaftar.id }
                    }
                    
                    // PERBAIKAN: Update totalItems agar jumlah pendaftar langsung berkurang
                    _state.update { currentState ->
                        currentState.copy(
                            totalItems = maxOf(0, currentState.totalItems - 1)
                        )
                    }
                } else {
                    _state.value = _state.value.copy(
                        error = "Gagal menghapus data. Status: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun importExcel(token: String?, file: File) {
        if (token == null) {
            _state.value = _state.value.copy(error = "Token tidak tersedia. Silakan login ulang.")
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, importMessage = null)
            
            try {
                val response = repository.importExcel(token, file)
                
                if (response.isSuccessful && response.body() != null) {
                    val importData = response.body()!!.data
                    val message = if (importData != null) {
                        "Berhasil mengimpor ${importData.imported} data peserta"
                    } else {
                        response.body()!!.message
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        importMessage = message,
                        error = null
                    )
                    // Reload list after import (silent - tidak tampilkan error jika gagal)
                    // Import sudah berhasil, jadi error reload tidak perlu ditampilkan
                    reloadListSilently(token)
                } else {
                    val errorBody = try {
                        response.errorBody()?.string()
                    } catch (e: IOException) {
                        null
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = errorBody ?: "Gagal mengimpor file. Status: ${response.code()}"
                    )
                }
            } catch (e: IOException) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Tidak dapat terhubung ke server. Pastikan server berjalan."
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    /**
     * Reload list dengan loading indicator (untuk reload setelah import)
     * Dengan delay dan retry mechanism untuk ensure data muncul
     */
    private fun reloadListSilently(token: String) {
        viewModelScope.launch {
            // Set loading state untuk tampilkan loading indicator
            _state.value = _state.value.copy(isReloading = true)
            
            // Delay sebelum reload untuk hindari race condition (server masih processing)
            delay(1500) // Wait 1.5 detik
            
            // Retry mechanism - coba 2 kali jika gagal
            var success = false
            repeat(2) { attempt ->
                if (success) return@repeat
                
                try {
                    val response = repository.getPesertaList(token, 1, 20)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val pendaftarResponses = body.data
                        val pagination = body.pagination
                        
                        _pendaftarList.value = pendaftarResponses.map { it.toPendaftar() }
                        
                        // Update pagination info
                        _state.value = _state.value.copy(
                            currentPage = pagination?.currentPage ?: 1,
                            totalPages = pagination?.lastPage ?: 1,
                            totalItems = pagination?.total ?: 0,
                            hasMore = pagination?.hasMore ?: false
                        )
                        
                        success = true
                        return@repeat // Success, exit retry loop
                    }
                } catch (e: Exception) {
                    // Jika bukan attempt terakhir, tunggu sebentar sebelum retry
                    if (attempt < 1) {
                        delay(1000) // Wait 1 detik sebelum retry
                    }
                }
            }
            
            // Clear loading state setelah selesai (berhasil atau gagal)
            _state.value = _state.value.copy(isReloading = false)
            
            // Silent fail - tidak tampilkan error jika masih gagal
            // Import sudah berhasil, user bisa reload manual nanti
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null, importMessage = null)
    }
    
    /**
     * FITUR BARU: Search pendaftar berdasarkan nama/NIM
     */
    fun searchPendaftar(token: String?, query: String) {
        // Update search query di state
        _state.value = _state.value.copy(searchQuery = query)
        
        // Reload list dengan search query (reset ke page 1)
        loadPendaftarList(token, page = 1, append = false, search = query.takeIf { it.isNotEmpty() })
    }
    
    /**
     * Clear search dan reload semua data
     */
    fun clearSearch(token: String?) {
        _state.value = _state.value.copy(searchQuery = "")
        loadPendaftarList(token, page = 1, append = false, search = null)
    }
}