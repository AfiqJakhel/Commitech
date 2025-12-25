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

data class Pendaftar(
    val id: Int,
    val nama: String,
    val nim: String,
    val divisi1: String,
    val alasan1: String,
    val divisi2: String,
    val alasan2: String,
    val krsTerakhir: String? = null,
    val formulirPendaftaran: String? = null,
    val suratKomitmen: String? = null
)

fun PendaftarResponse.toPendaftar(): Pendaftar {
    return Pendaftar(
        id = id,
        nama = nama ?: "Nama Tidak Diketahui",
        nim = nim ?: "",
        divisi1 = pilihanDivisi1 ?: "",
        alasan1 = alasan1 ?: "",
        divisi2 = pilihanDivisi2 ?: "",
        alasan2 = alasan2 ?: "",
        krsTerakhir = krsTerakhir,
        formulirPendaftaran = formulirPendaftaran?.takeIf { it.isNotBlank() },
        suratKomitmen = suratKomitmen?.takeIf { it.isNotBlank() }
    )
}

data class DataPendaftarState(
    val isLoading: Boolean = false,
    val isReloading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val importMessage: String? = null,
    val importErrors: List<String>? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasMore: Boolean = false,
    val searchQuery: String = ""
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

            val isReloadAfterImport = _state.value.importMessage != null
            
            if (!isReloadAfterImport && !append) {
                _state.value = _state.value.copy(isLoading = true, error = null)
            } else if (append) {
                _state.value = _state.value.copy(isLoadingMore = true, error = null)
            }

            var success = false
            var lastError: String? = null
            val maxRetries = 3
            
            val searchQueryParam = search?.takeIf { it.isNotBlank() }
                ?: _state.value.searchQuery.takeIf { it.isNotEmpty() }
            
            repeat(maxRetries) { attempt ->
                if (success) return@repeat
                
                try {
                    val response = repository.getPesertaList(token, page, 20, searchQueryParam)
                    
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val pendaftarResponses = body.data
                        val pagination = body.pagination

                        val currentPage = pagination?.currentPage ?: page
                        val totalPages = pagination?.lastPage ?: 1
                        val totalItems = pagination?.total ?: 0
                        val hasMore = pagination?.hasMore ?: false

                        if (append) {
                            _pendaftarList.value += pendaftarResponses.map { it.toPendaftar() }
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
                            hasMore = hasMore,
                            searchQuery = searchQueryParam ?: ""
                        )
                        success = true
                        return@repeat
                    } else {
                        val errorBody = try {
                            response.errorBody()?.string()
                        } catch (_: IOException) {
                            null
                        }
                        lastError = errorBody ?: "Gagal memuat data. Status: ${response.code()}"

                        if (response.code() in 400..599) {
                            success = false
                            return@repeat
                        }
                    }
                } catch (_: IOException) {
                    lastError = "Tidak dapat terhubung ke server. Pastikan server berjalan."

                    if (attempt < maxRetries - 1) {
                        val delayMs = (500 * (1 shl attempt)).coerceAtMost(2000)
                        delay(delayMs.toLong())
                    }
                } catch (e: Exception) {
                    lastError = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"

                    if (attempt < maxRetries - 1) {
                        val delayMs = (500 * (1 shl attempt)).coerceAtMost(2000)
                        delay(delayMs.toLong())
                    }
                }
            }

            if (!success && lastError != null) {
                if (isReloadAfterImport) {
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

    fun deletePendaftar(token: String?, pendaftar: Pendaftar) {
        if (token == null) {
            _state.value = _state.value.copy(error = "Token tidak tersedia. Silakan login ulang.")
            return
        }
        
        viewModelScope.launch {
            try {
                val response = repository.deletePeserta(token, pendaftar.id)
                
                if (response.isSuccessful) {
                    _pendaftarList.update { listSaatIni ->
                        listSaatIni.filterNot { it.id == pendaftar.id }
                    }

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
                    reloadListSilently(token)
                } else {
                    val errorBody = try {
                        response.errorBody()?.string()
                    } catch (_: IOException) {
                        null
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = errorBody ?: "Gagal mengimpor file. Status: ${response.code()}"
                    )
                }
            } catch (_: IOException) {
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

    private fun reloadListSilently(token: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isReloading = true)

            delay(1500)

            var success = false
            repeat(2) { attempt ->
                if (success) return@repeat
                
                try {
                    val response = repository.getPesertaList(
                        token,
                        1,
                        20,
                        _state.value.searchQuery.takeIf { it.isNotEmpty() }
                    )
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val pendaftarResponses = body.data
                        val pagination = body.pagination
                        
                        _pendaftarList.value = pendaftarResponses.map { it.toPendaftar() }

                        _state.value = _state.value.copy(
                            currentPage = pagination?.currentPage ?: 1,
                            totalPages = pagination?.lastPage ?: 1,
                            totalItems = pagination?.total ?: 0,
                            hasMore = pagination?.hasMore ?: false
                        )
                        
                        success = true
                        return@repeat
                    }
                } catch (_: Exception) {
                    if (attempt < 1) {
                        delay(1000)
                    }
                }
            }

            _state.value = _state.value.copy(isReloading = false)
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null, importMessage = null)
    }

    fun searchPendaftar(token: String?, query: String) {
        val normalizedQuery = query.trim()
        _state.value = _state.value.copy(searchQuery = normalizedQuery)

        loadPendaftarList(token, page = 1, append = false, search = normalizedQuery.takeIf { it.isNotEmpty() })
    }

    fun clearSearch(token: String?) {
        _state.value = _state.value.copy(searchQuery = "")
        loadPendaftarList(token, page = 1, append = false, search = null)
    }
}