package com.example.commitech.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.commitech.data.model.PendaftarResponse
import com.example.commitech.data.repository.DataPendaftarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

data class Peserta(
    val id: Int? = null, // ID dari database (untuk API call)
    val nama: String,
    val lulusBerkas: Boolean,
    val ditolak: Boolean,
    val statusSeleksiBerkas: String? = null // Status dari database: belum_direview, lulus, tidak_lulus
)

class SeleksiBerkasViewModel : ViewModel() {
    private val repository = DataPendaftarRepository()
    private var authToken: String = ""
    
    private val _pesertaList = MutableStateFlow<List<Peserta>>(emptyList())
    val pesertaList: StateFlow<List<Peserta>> = _pesertaList.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Daftar peserta yang sudah diterima (lulus berkas)
    // Hanya peserta dengan status_seleksi_berkas == "lulus" yang ditampilkan
    val pesertaDiterima: List<Peserta> get() = _pesertaList.value.filter { 
        it.statusSeleksiBerkas == "lulus" 
    }

    /**
     * Set auth token dan load data dari database
     */
    fun setAuthToken(token: String) {
        authToken = token
        loadPesertaFromDatabase()
    }

    /**
     * Load peserta dari database via API
     * 
     * CATATAN PENTING:
     * - Menggunakan endpoint yang SAMA dengan Data Pendaftar: GET /api/peserta
     * - Mengambil data dari tabel database yang SAMA: tabel `peserta`
     * - Tapi fungsi dan tampilannya BERBEDA:
     *   * Data Pendaftar: menampilkan semua data peserta untuk CRUD
     *   * Seleksi Berkas: untuk proses seleksi berkas dengan button Terima/Tolak
     * 
     * Semua peserta diinisialisasi dengan status belum direview (lulusBerkas=false, ditolak=false)
     * Button Terima/Tolak akan muncul untuk semua peserta
     */
    fun loadPesertaFromDatabase() {
        if (!isTokenValid()) {
            Log.w("SeleksiBerkasViewModel", "AuthToken tidak valid, tidak bisa load peserta dari database")
            // Jangan tampilkan error jika token belum tersedia (mungkin masih loading)
            // Error hanya ditampilkan jika sudah mencoba load tapi gagal
            _isLoading.value = false
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Retry mechanism untuk handle intermittent connection issues
            var success = false
            var lastError: String? = null
            val maxRetries = 3 // Coba maksimal 3 kali
            
            repeat(maxRetries) { attempt ->
                if (success) return@repeat
                
                try {
                    Log.d("SeleksiBerkasViewModel", "Memulai load peserta dari database... (Attempt ${attempt + 1}/$maxRetries)")
                    
                    // Load semua peserta dengan pagination
                    val allPeserta = mutableListOf<PendaftarResponse>()
                    var currentPage = 1
                    var hasMore = true
                    var maxPages = 10 // Safety limit untuk menghindari infinite loop
                    
                    while (hasMore && currentPage <= maxPages) {
                        Log.d("SeleksiBerkasViewModel", "Loading page $currentPage...")
                        
                        // Menggunakan endpoint yang sama dengan Data Pendaftar: GET /api/peserta
                        // Mengambil data dari tabel database yang sama: tabel `peserta`
                        val response = withContext(Dispatchers.IO) {
                            repository.getPesertaList(authToken, currentPage, 100) // Load 100 per page
                        }
                        
                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            val pageData = body.data
                            
                            Log.d("SeleksiBerkasViewModel", "Page $currentPage: ${pageData.size} peserta")
                            
                            if (pageData.isEmpty()) {
                                hasMore = false
                            } else {
                                allPeserta.addAll(pageData)
                                
                                // Check if there's more pages
                                val pagination = body.pagination
                                hasMore = pagination?.let { 
                                    it.currentPage < it.lastPage 
                                } ?: false
                                
                                currentPage++
                            }
                        } else {
                            val errorBody = try {
                                response.errorBody()?.string()
                            } catch (e: IOException) {
                                null
                            }
                            Log.e("SeleksiBerkasViewModel", "Gagal load page $currentPage: ${response.code()} - ${response.message()}")
                            Log.e("SeleksiBerkasViewModel", "Error body: $errorBody")
                            
                            // Jika HTTP error (4xx/5xx), tidak perlu retry untuk page ini
                            // Tapi jika sudah ada data, lanjutkan dengan data yang sudah di-load
                            if (allPeserta.isEmpty() && response.code() in 400..599) {
                                // HTTP error dan belum ada data - tidak perlu retry
                                lastError = "Gagal memuat data peserta. Status: ${response.code()}. ${errorBody ?: ""}"
                                hasMore = false
                                success = false
                                return@repeat
                            } else if (allPeserta.isNotEmpty()) {
                                // Jika sudah ada data, lanjutkan dengan data yang sudah di-load
                                Log.w("SeleksiBerkasViewModel", "Ada error tapi sudah ada ${allPeserta.size} peserta, lanjutkan...")
                                hasMore = false
                            } else {
                                // Network error atau timeout - bisa retry
                                throw IOException("Network error: ${response.code()}")
                            }
                        }
                    }
                    
                    Log.d("SeleksiBerkasViewModel", "Total peserta yang di-load: ${allPeserta.size}")
                    
                    // Convert PendaftarResponse ke Peserta
                    // Menggunakan status_seleksi_berkas dari database (enum: belum_direview, lulus, tidak_lulus)
                    val pesertaList = allPeserta.map { pendaftar ->
                        val status = pendaftar.statusSeleksiBerkas ?: "belum_direview"
                        
                        // Mapping status enum ke boolean untuk kompatibilitas dengan UI
                        val lulusBerkas = status == "lulus"
                        val ditolak = status == "tidak_lulus"
                        
                        Peserta(
                            id = pendaftar.id,
                            nama = pendaftar.nama ?: "Nama tidak diketahui",
                            lulusBerkas = lulusBerkas,
                            ditolak = ditolak,
                            statusSeleksiBerkas = status // Simpan status asli dari database
                        )
                    }
                    
                    _pesertaList.value = pesertaList
                    _isLoading.value = false
                    _error.value = null // Clear error jika berhasil
                    
                    Log.d("SeleksiBerkasViewModel", "✅ Berhasil load ${pesertaList.size} peserta dari database")
                    Log.d("SeleksiBerkasViewModel", "   - Peserta lulus berkas: ${pesertaList.count { it.lulusBerkas }}")
                    Log.d("SeleksiBerkasViewModel", "   - Peserta ditolak: ${pesertaList.count { it.ditolak }}")
                    Log.d("SeleksiBerkasViewModel", "   - Peserta belum direview: ${pesertaList.count { !it.lulusBerkas && !it.ditolak }}")
                    Log.d("SeleksiBerkasViewModel", "   - Peserta diterima (untuk seleksi wawancara): ${pesertaList.count { it.lulusBerkas && !it.ditolak }}")
                    
                    success = true
                    return@repeat // Success, exit retry loop
                    
                } catch (e: IOException) {
                    // Network error - bisa retry
                    lastError = "Tidak dapat terhubung ke server. Pastikan server berjalan."
                    Log.e("SeleksiBerkasViewModel", "Network error saat load peserta (Attempt ${attempt + 1}/$maxRetries)", e)
                    
                    // Jika bukan attempt terakhir, tunggu sebentar sebelum retry
                    if (attempt < maxRetries - 1) {
                        // Exponential backoff: 500ms, 1000ms, 2000ms
                        val delayMs = (500 * (1 shl attempt)).coerceAtMost(2000)
                        kotlinx.coroutines.delay(delayMs.toLong())
                    }
                } catch (e: Exception) {
                    // Unexpected error
                    lastError = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
                    Log.e("SeleksiBerkasViewModel", "Error saat load peserta dari database (Attempt ${attempt + 1}/$maxRetries)", e)
                    
                    // Jika bukan attempt terakhir, tunggu sebentar sebelum retry
                    if (attempt < maxRetries - 1) {
                        val delayMs = (500 * (1 shl attempt)).coerceAtMost(2000)
                        kotlinx.coroutines.delay(delayMs.toLong())
                    }
                }
            }
            
            // Jika semua retry gagal, tampilkan error
            if (!success && lastError != null) {
                _error.value = lastError
                _isLoading.value = false
                Log.e("SeleksiBerkasViewModel", "Gagal load peserta setelah $maxRetries attempts: $lastError")
            }
        }
    }
    
    /**
     * Update status peserta (lulus berkas atau ditolak)
     * Update local state dan simpan ke database via API
     */
    fun updatePesertaStatus(nama: String, lulusBerkas: Boolean, ditolak: Boolean) {
        // Tentukan status baru berdasarkan boolean
        val statusBaru = when {
            lulusBerkas -> "lulus"
            ditolak -> "tidak_lulus"
            else -> "belum_direview"
        }
        
        // Update local state untuk immediate UI update
        _pesertaList.value = _pesertaList.value.map { 
            if (it.nama == nama) {
                it.copy(
                    lulusBerkas = lulusBerkas, 
                    ditolak = ditolak,
                    statusSeleksiBerkas = statusBaru
                )
            } else {
                it
            }
        }
        
        // Simpan ke database via API
        val peserta = _pesertaList.value.find { it.nama == nama }
        if (authToken.isNotBlank() && peserta?.id != null) {
            saveStatusToDatabase(peserta.id!!, lulusBerkas)
        } else {
            Log.w("SeleksiBerkasViewModel", "Tidak bisa simpan status ke database: authToken=${authToken.isNotBlank()}, pesertaId=${peserta?.id}")
        }
        
        Log.d("SeleksiBerkasViewModel", "Status peserta '$nama' diupdate: status=$statusBaru, lulusBerkas=$lulusBerkas, ditolak=$ditolak")
    }
    
    /**
     * Simpan status seleksi berkas ke database
     */
    private fun saveStatusToDatabase(pesertaId: Int, lulusBerkas: Boolean) {
        viewModelScope.launch {
            try {
                val status = if (lulusBerkas) "lulus" else "tidak_lulus"
                val request = com.example.commitech.data.model.UpdateStatusSeleksiBerkasRequest(status)
                
                Log.d("SeleksiBerkasViewModel", "Menyimpan status seleksi berkas untuk peserta $pesertaId: $status")
                
                val response = withContext(Dispatchers.IO) {
                    repository.updateStatusSeleksiBerkas(authToken, pesertaId, request)
                }
                
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("SeleksiBerkasViewModel", "✅ Status seleksi berkas berhasil disimpan: ${body?.message}")
                    // Tidak perlu refresh, karena UI sudah diupdate di updatePesertaStatus
                } else {
                    Log.e("SeleksiBerkasViewModel", "❌ Gagal simpan status seleksi berkas: ${response.code()} - ${response.message()}")
                    response.errorBody()?.string()?.let { 
                        Log.e("SeleksiBerkasViewModel", "Error body: $it") 
                    }
                    _error.value = "Gagal menyimpan status seleksi berkas. Status: ${response.code()}"
                }
            } catch (e: IOException) {
                Log.e("SeleksiBerkasViewModel", "Network error saat simpan status seleksi berkas", e)
                _error.value = "Tidak dapat terhubung ke server. Pastikan server berjalan."
            } catch (e: Exception) {
                Log.e("SeleksiBerkasViewModel", "Error saat simpan status seleksi berkas", e)
                _error.value = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Check if token is still valid before loading
     */
    private fun isTokenValid(): Boolean {
        return authToken.isNotBlank() && authToken.length > 10 // Basic validation
    }
    
    /**
     * Refresh data dari database
     */
    fun refresh() {
        loadPesertaFromDatabase()
    }
}
