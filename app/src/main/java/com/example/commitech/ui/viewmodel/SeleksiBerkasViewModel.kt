package com.example.commitech.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.commitech.data.model.PendaftarResponse
import com.example.commitech.data.model.UpdateStatusSeleksiBerkasRequest
import com.example.commitech.data.repository.DataPendaftarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

data class Peserta(
    val id: Int? = null, // ID dari database (untuk API call)
    val nama: String,
    val nim: String? = null,
    val email: String? = null,
    val telepon: String? = null,
    val jurusan: String? = null,
    val angkatan: String? = null,
    val divisi1: String? = null,
    val alasan1: String? = null,
    val divisi2: String? = null,
    val alasan2: String? = null,
    val krsTerakhir: String? = null, // Link KRS
    val formulirPendaftaran: String? = null, // Link atau status formulir
    val suratKomitmen: String? = null, // Link atau status surat komitmen
    val lulusBerkas: Boolean,
    val ditolak: Boolean,
    val statusSeleksiBerkas: String? = null // Status dari database: belum_direview, lulus, tidak_lulus
)

data class SeleksiBerkasState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasMore: Boolean = false
)

class SeleksiBerkasViewModel : ViewModel() {
    private val repository = DataPendaftarRepository()
    private var authToken: String = ""
    
    private val _pesertaList = MutableStateFlow<List<Peserta>>(emptyList())
    val pesertaList: StateFlow<List<Peserta>> = _pesertaList.asStateFlow()
    
    private val _state = MutableStateFlow(SeleksiBerkasState())
    val state: StateFlow<SeleksiBerkasState> = _state.asStateFlow()
    
    // Convenience getters untuk backward compatibility dengan UI yang sudah ada
    val isLoading: StateFlow<Boolean> = _state.map { it.isLoading }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _state.value.isLoading
    )
    
    val error: StateFlow<String?> = _state.map { it.error }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _state.value.error
    )
    
    // Daftar peserta yang sudah diterima (lulus berkas)
    // Hanya peserta dengan status_seleksi_berkas == "lulus" yang ditampilkan
    val pesertaDiterima: List<Peserta> get() = _pesertaList.value.filter { 
        it.statusSeleksiBerkas == "lulus" 
    }
    
    // Cek apakah semua peserta sudah direview
    val semuaPesertaSudahDireview: Boolean get() = _pesertaList.value.isNotEmpty() && 
        _pesertaList.value.all { it.statusSeleksiBerkas != null && it.statusSeleksiBerkas != "belum_direview" }

    /**
     * Set auth token dan load data dari database
     */
    fun setAuthToken(token: String) {
        authToken = token
        loadPesertaFromDatabase()
    }

    /**
     * Load peserta dari database via API dengan pagination
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
    fun loadPesertaFromDatabase(page: Int = 1, append: Boolean = false) {
        if (!isTokenValid()) {
            Log.w("SeleksiBerkasViewModel", "AuthToken tidak valid, tidak bisa load peserta dari database")
            _state.value = _state.value.copy(isLoading = false)
            return
        }
        
        viewModelScope.launch {
            if (!append) {
                _state.value = _state.value.copy(isLoading = true, error = null)
            }
            
            // Retry mechanism untuk handle intermittent connection issues
            var success = false
            var lastError: String? = null
            val maxRetries = 3 // Coba maksimal 3 kali
            
            repeat(maxRetries) { attempt ->
                if (success) return@repeat
                
                try {
                    Log.d("SeleksiBerkasViewModel", "Memulai load peserta dari database... Page: $page (Attempt ${attempt + 1}/$maxRetries)")
                    
                    // Load peserta dengan pagination (20 per page seperti Data Pendaftar)
                    val response = withContext(Dispatchers.IO) {
                        repository.getPesertaList(authToken, page, 20)
                    }
                    
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val pageData = body.data
                        val pagination = body.pagination
                        
                        Log.d("SeleksiBerkasViewModel", "Page $page: ${pageData.size} peserta")
                        
                        // Convert PendaftarResponse ke Peserta dengan data lengkap
                        val pesertaList = pageData.map { pendaftar ->
                            // Ambil status dari API response atau default ke "belum_direview"
                            val status = pendaftar.statusSeleksiBerkas ?: "belum_direview"
                            val lulusBerkas = status == "lulus"
                            val ditolak = status == "tidak_lulus"
                            
                            Peserta(
                                id = pendaftar.id,
                                nama = pendaftar.nama ?: "Nama tidak diketahui",
                                nim = pendaftar.nim,
                                email = pendaftar.email,
                                telepon = pendaftar.telepon,
                                jurusan = pendaftar.jurusan,
                                angkatan = pendaftar.angkatan,
                                divisi1 = pendaftar.pilihanDivisi1,
                                alasan1 = pendaftar.alasan1,
                                divisi2 = pendaftar.pilihanDivisi2,
                                alasan2 = pendaftar.alasan2,
                                krsTerakhir = pendaftar.krsTerakhir,
                                formulirPendaftaran = if (pendaftar.formulirPendaftaran == true) "Tersedia" else pendaftar.formulirPendaftaran?.toString(),
                                suratKomitmen = if (pendaftar.suratKomitmen == true) "Tersedia" else pendaftar.suratKomitmen?.toString(),
                                lulusBerkas = lulusBerkas,
                                ditolak = ditolak,
                                statusSeleksiBerkas = status
                            )
                        }
                        
                        // Update pagination info
                        val currentPage = pagination?.currentPage ?: page
                        val totalPages = pagination?.lastPage ?: 1
                        val totalItems = pagination?.total ?: 0
                        val hasMore = pagination?.hasMore ?: false
                        
                        // Append atau replace list
                        if (append) {
                            _pesertaList.value = _pesertaList.value + pesertaList
                        } else {
                            _pesertaList.value = pesertaList
                        }
                        
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = null,
                            currentPage = currentPage,
                            totalPages = totalPages,
                            totalItems = totalItems,
                            hasMore = hasMore
                        )
                        
                        Log.d("SeleksiBerkasViewModel", "✅ Berhasil load ${pesertaList.size} peserta dari database (Page $currentPage/$totalPages)")
                        Log.d("SeleksiBerkasViewModel", "   - Total peserta: $totalItems")
                        Log.d("SeleksiBerkasViewModel", "   - Peserta lulus berkas: ${_pesertaList.value.count { it.lulusBerkas }}")
                        Log.d("SeleksiBerkasViewModel", "   - Peserta ditolak: ${_pesertaList.value.count { it.ditolak }}")
                        Log.d("SeleksiBerkasViewModel", "   - Peserta belum direview: ${_pesertaList.value.count { !it.lulusBerkas && !it.ditolak }}")
                        
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
                        if (response.code() in 400..599) {
                            success = false
                            return@repeat
                        }
                    }
                } catch (e: IOException) {
                    // Network error - bisa retry
                    lastError = "Tidak dapat terhubung ke server. Pastikan server berjalan."
                    Log.e("SeleksiBerkasViewModel", "Network error saat load peserta (Attempt ${attempt + 1}/$maxRetries)", e)
                    
                    // Jika bukan attempt terakhir, tunggu sebentar sebelum retry
                    if (attempt < maxRetries - 1) {
                        // Exponential backoff: 500ms, 1000ms, 2000ms
                        val delayMs = (500 * (1 shl attempt)).coerceAtMost(2000)
                        delay(delayMs.toLong())
                    }
                } catch (e: Exception) {
                    // Unexpected error
                    lastError = "Terjadi kesalahan: ${e.message ?: "Unknown error"}"
                    Log.e("SeleksiBerkasViewModel", "Error saat load peserta dari database (Attempt ${attempt + 1}/$maxRetries)", e)
                    
                    // Jika bukan attempt terakhir, tunggu sebentar sebelum retry
                    if (attempt < maxRetries - 1) {
                        val delayMs = (500 * (1 shl attempt)).coerceAtMost(2000)
                        delay(delayMs.toLong())
                    }
                }
            }
            
            // Jika semua retry gagal, tampilkan error
            if (!success && lastError != null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = lastError
                )
                Log.e("SeleksiBerkasViewModel", "Gagal load peserta setelah $maxRetries attempts: $lastError")
            }
        }
    }
    
    fun loadNextPage() {
        val currentState = _state.value
        if (currentState.hasMore && !currentState.isLoading) {
            loadPesertaFromDatabase(currentState.currentPage + 1, append = false)
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
        _pesertaList.update { listSaatIni ->
            listSaatIni.map { pesertaLama: Peserta ->
                if (pesertaLama.nama == nama) {
                    pesertaLama.copy(
                        lulusBerkas = lulusBerkas, 
                        ditolak = ditolak,
                        statusSeleksiBerkas = statusBaru
                    )
                } else {
                    pesertaLama
                }
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
                val request = UpdateStatusSeleksiBerkasRequest(status)
                
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
                    _state.value = _state.value.copy(error = "Gagal menyimpan status seleksi berkas. Status: ${response.code()}")
                }
            } catch (e: IOException) {
                Log.e("SeleksiBerkasViewModel", "Network error saat simpan status seleksi berkas", e)
                _state.value = _state.value.copy(error = "Tidak dapat terhubung ke server. Pastikan server berjalan.")
            } catch (e: Exception) {
                Log.e("SeleksiBerkasViewModel", "Error saat simpan status seleksi berkas", e)
                _state.value = _state.value.copy(error = "Terjadi kesalahan: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    /**
     * Check if token is still valid before loading
     */
    private fun isTokenValid(): Boolean {
        return authToken.isNotBlank() && authToken.length > 10 // Basic validation
    }
    
    /**
     * Refresh data dari database (reset ke page 1)
     */
    fun refresh() {
        loadPesertaFromDatabase(page = 1, append = false)
    }
    
    /**
     * Edit data pendaftar
     */
    fun editPendaftar(token: String?, pesertaBaru: Peserta) {
        if (token == null || pesertaBaru.id == null) {
            _state.update { it.copy(error = "Token atau ID peserta tidak tersedia") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val pendaftarResponse = PendaftarResponse(
                    id = pesertaBaru.id!!,
                    nama = pesertaBaru.nama,
                    nim = pesertaBaru.nim,
                    email = pesertaBaru.email,
                    telepon = pesertaBaru.telepon,
                    jurusan = pesertaBaru.jurusan,
                    angkatan = pesertaBaru.angkatan,
                    pilihanDivisi1 = pesertaBaru.divisi1,
                    pilihanDivisi2 = pesertaBaru.divisi2,
                    pilihanDivisi3 = null,
                    alasan1 = pesertaBaru.alasan1,
                    alasan2 = pesertaBaru.alasan2,
                    alasan3 = null,
                    krsTerakhir = pesertaBaru.krsTerakhir,
                    formulirPendaftaran = pesertaBaru.formulirPendaftaran?.toBoolean(),
                    suratKomitmen = pesertaBaru.suratKomitmen?.toBoolean(),
                    pindahDivisi = null,
                    tanggalJadwal = null,
                    waktuJadwal = null,
                    lokasi = null,
                    statusSeleksiBerkas = null
                )
                
                val response = withContext(Dispatchers.IO) {
                    repository.updatePeserta(token, pesertaBaru.id, pendaftarResponse)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    // Update local list
                    _pesertaList.update { listSaatIni ->
                        listSaatIni.map { pesertaLama: Peserta ->
                            if (pesertaLama.id == pesertaBaru.id) {
                                pesertaBaru
                            } else {
                                pesertaLama
                            }
                        }
                    }
                    _state.update { it.copy(isLoading = false, error = null) }
                    Log.d("SeleksiBerkasViewModel", "✅ Data peserta berhasil diupdate")
                } else {
                    _state.update { it.copy(isLoading = false, error = "Gagal mengupdate data. Status: ${response.code()}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Terjadi kesalahan: ${e.message ?: "Unknown error"}") }
                Log.e("SeleksiBerkasViewModel", "Error saat update peserta", e)
            }
        }
    }
    
    /**
     * Delete data pendaftar
     */
    fun deletePendaftar(token: String?, peserta: Peserta) {
        if (token == null || peserta.id == null) {
            _state.update { it.copy(error = "Token atau ID peserta tidak tersedia") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.deletePeserta(token, peserta.id!!)
                }
                
                if (response.isSuccessful) {
                    // Remove from local list
                    _pesertaList.update { listSaatIni ->
                        listSaatIni.filterNot { pesertaItem: Peserta ->
                            pesertaItem.id == peserta.id
                        }
                    }
                    Log.d("SeleksiBerkasViewModel", "✅ Peserta berhasil dihapus")
                    // Update totalItems
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            totalItems = maxOf(0, currentState.totalItems - 1)
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Gagal menghapus data. Status: ${response.code()}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Terjadi kesalahan: ${e.message ?: "Unknown error"}") }
                Log.e("SeleksiBerkasViewModel", "Error saat delete peserta", e)
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

