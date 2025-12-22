package com.example.commitech.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.commitech.data.api.ApiService
import com.example.commitech.data.api.RetrofitClient
import com.example.commitech.data.model.JadwalRekrutmenItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Import untuk Peserta
import com.example.commitech.ui.viewmodel.Peserta
import kotlinx.coroutines.delay
import kotlinx.coroutines.time.delay

data class Jadwal(
    val id: Int,
    val judul: String,
    val tanggalMulai: String,
    val tanggalSelesai: String,
    val waktuMulai: String,
    val waktuSelesai: String,
    val pewawancara: String
)

class JadwalViewModel : ViewModel() {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("in", "ID"))
    private val api: ApiService = RetrofitClient.apiService

    private var authToken: String = ""

    private val _daftarJadwal = mutableStateListOf<Jadwal>()
    val daftarJadwal: List<Jadwal> get() = _daftarJadwal
    
    // State untuk menyimpan peserta yang dipilih per jadwal
    // Key: jadwalId, Value: List<Peserta>
    private val _pesertaPerJadwal = mutableMapOf<Int, MutableList<Peserta>>()
    val pesertaPerJadwal: Map<Int, List<Peserta>> get() = _pesertaPerJadwal
    
    // StateFlow untuk trigger recomposition ketika pesertaPerJadwal berubah
    private val _pesertaPerJadwalUpdateTrigger = kotlinx.coroutines.flow.MutableStateFlow(0)
    val pesertaPerJadwalUpdateTrigger: kotlinx.coroutines.flow.StateFlow<Int> = _pesertaPerJadwalUpdateTrigger.asStateFlow()
    
    fun getPesertaByJadwalId(jadwalId: Int): List<Peserta> {
        return _pesertaPerJadwal[jadwalId] ?: emptyList()
    }
    
    /**
     * Mendapatkan semua peserta yang sudah ada di jadwal manapun (kecuali jadwal tertentu)
     * Digunakan untuk filter peserta yang sudah terdaftar di jadwal lain
     */
    fun getAllPesertaDiJadwalLain(kecualiJadwalId: Int): Set<Int> {
        return _pesertaPerJadwal
            .filterKeys { it != kecualiJadwalId }
            .values
            .flatten()
            .mapNotNull { it.id }
            .toSet()
    }
    
    /**
     * Mendapatkan semua peserta yang sudah ada di jadwal manapun berdasarkan nama
     */
    fun getAllPesertaNamaDiJadwalLain(kecualiJadwalId: Int): Set<String> {
        return _pesertaPerJadwal
            .filterKeys { it != kecualiJadwalId }
            .values
            .flatten()
            .map { it.nama }
            .toSet()
    }
    
    fun tambahPesertaKeJadwal(jadwalId: Int, peserta: Peserta) {
        val currentList = _pesertaPerJadwal.getOrPut(jadwalId) { mutableListOf() }
        // Cek apakah peserta sudah ada
        if (!currentList.any { it.nama == peserta.nama }) {
            // Cek maksimal 5 peserta
            if (currentList.size < 5) {
                currentList.add(peserta)
                // Trigger recomposition
                _pesertaPerJadwalUpdateTrigger.value++
            }
        }
    }
    
    fun hapusPesertaDariJadwal(jadwalId: Int, namaPeserta: String) {
        val peserta = _pesertaPerJadwal[jadwalId]?.find { it.nama == namaPeserta }
        
        // Hapus dari local state dulu untuk immediate UI update
        _pesertaPerJadwal[jadwalId]?.removeAll { it.nama == namaPeserta }
        // Trigger recomposition setelah menghapus
        _pesertaPerJadwalUpdateTrigger.value++
        
        // Hapus dari database juga jika ada ID
        if (authToken.isNotBlank() && peserta?.id != null) {
            removePesertaFromJadwal(jadwalId, peserta.id)
        } else {
            Log.w("JadwalViewModel", "Tidak bisa hapus peserta dari database: authToken=${authToken.isNotBlank()}, pesertaId=${peserta?.id}")
        }
    }
    
    private fun removePesertaFromJadwal(jadwalId: Int, pesertaId: Int) {
        viewModelScope.launch {
            try {
                Log.d("JadwalViewModel", "Menghapus peserta $pesertaId dari jadwal $jadwalId")
                
                val resp = withContext(Dispatchers.IO) {
                    api.removePesertaFromJadwal("Bearer $authToken", jadwalId, pesertaId)
                }
                
                if (resp.isSuccessful) {
                    Log.d("JadwalViewModel", "Peserta berhasil dihapus dari jadwal")
                } else {
                    Log.e("JadwalViewModel", "Gagal hapus peserta dari jadwal: ${resp.code()}")
                }
            } catch (e: Exception) {
                Log.e("JadwalViewModel", "Error saat hapus peserta dari jadwal", e)
            }
        }
    }
    
    fun setPesertaUntukJadwal(jadwalId: Int, pesertaList: List<Peserta>) {
        // Maksimal 5 peserta
        val pesertaTerbatas = pesertaList.take(5)
        _pesertaPerJadwal[jadwalId] = pesertaTerbatas.toMutableList()
        // Trigger recomposition
        _pesertaPerJadwalUpdateTrigger.value++
        
        // Simpan ke database via API
        if (authToken.isNotBlank() && pesertaTerbatas.isNotEmpty()) {
            savePesertaToJadwal(jadwalId, pesertaTerbatas)
        } else if (pesertaTerbatas.isEmpty()) {
            // Jika tidak ada peserta yang dipilih, hapus semua peserta dari jadwal di database
            // (optional: bisa dihapus jika ingin membiarkan peserta lama tetap ada)
            Log.d("JadwalViewModel", "Tidak ada peserta yang dipilih untuk jadwal $jadwalId")
        }
    }
    
    /**
     * Assign peserta ke jadwal menggunakan List<Int> (peserta IDs)
     * Fungsi ini akan menambahkan peserta yang dipilih ke peserta yang sudah ada di jadwal
     */
    fun assignPesertaToJadwal(
        token: String,
        jadwalId: Int,
        pesertaIds: List<Int>,
        onComplete: () -> Unit = {}
    ) {
        authToken = token
        viewModelScope.launch {
            try {
                // Load peserta yang sudah ada di jadwal untuk validasi
                val currentPesertaResp = withContext(Dispatchers.IO) {
                    api.getPesertaByJadwal("Bearer $token", jadwalId)
                }
                
                var currentCount = 0
                if (currentPesertaResp.isSuccessful) {
                    currentPesertaResp.body()?.data?.let {
                        currentCount = it.size
                    }
                }
                
                // Validasi maksimal 5 peserta total (peserta lama + baru)
                val totalPeserta = currentCount + pesertaIds.size
                
                if (totalPeserta > 5) {
                    Log.e("JadwalViewModel", "Total peserta melebihi batas maksimal 5. Current: $currentCount, New: ${pesertaIds.size}, Total: $totalPeserta")
                    return@launch
                }
                
                val request = com.example.commitech.data.model.AssignPesertaRequest(pesertaIds)
                
                Log.d("JadwalViewModel", "Assigning ${pesertaIds.size} peserta ke jadwal $jadwalId (Current: $currentCount, Total akan: $totalPeserta): $pesertaIds")
                
                val resp = withContext(Dispatchers.IO) {
                    api.assignPesertaToJadwal("Bearer $token", jadwalId, request)
                }
                
                if (resp.isSuccessful) {
                    val body = resp.body()
                    Log.d("JadwalViewModel", "✅ Peserta berhasil di-assign ke jadwal: ${body?.pesan}")
                    
                    // Refresh peserta dari jadwal setelah berhasil
                    loadPesertaFromJadwal(jadwalId)
                    onComplete()
                } else {
                    Log.e("JadwalViewModel", "❌ Gagal assign peserta ke jadwal: ${resp.code()} - ${resp.message()}")
                    resp.errorBody()?.string()?.let { 
                        Log.e("JadwalViewModel", "Error body: $it") 
                    }
                }
            } catch (e: Exception) {
                Log.e("JadwalViewModel", "❌ Error saat assign peserta ke jadwal", e)
            }
        }
    }
    
    private fun savePesertaToJadwal(jadwalId: Int, pesertaList: List<Peserta>) {
        viewModelScope.launch {
            try {
                // Filter peserta yang memiliki ID (untuk API call)
                val pesertaIds = pesertaList.mapNotNull { it.id }
                val pesertaTanpaId = pesertaList.filter { it.id == null }
                
                if (pesertaTanpaId.isNotEmpty()) {
                    Log.w("JadwalViewModel", "Beberapa peserta tidak memiliki ID dan akan dilewati: ${pesertaTanpaId.map { it.nama }}")
                }
                
                if (pesertaIds.isEmpty()) {
                    Log.e("JadwalViewModel", "Tidak ada peserta dengan ID yang valid untuk disimpan ke jadwal $jadwalId")
                    Log.e("JadwalViewModel", "Peserta yang dipilih: ${pesertaList.map { "${it.nama} (ID: ${it.id})" }}")
                    return@launch
                }
                
                val request = com.example.commitech.data.model.AssignPesertaRequest(pesertaIds)
                
                Log.d("JadwalViewModel", "Mengirim ${pesertaIds.size} peserta ke jadwal $jadwalId: $pesertaIds")
                
                val resp = withContext(Dispatchers.IO) {
                    api.assignPesertaToJadwal("Bearer $authToken", jadwalId, request)
                }
                
                if (resp.isSuccessful) {
                    val body = resp.body()
                    Log.d("JadwalViewModel", "✅ Peserta berhasil di-assign ke jadwal: ${body?.pesan}")
                    Log.d("JadwalViewModel", "Data: ${body?.data}")
                    
                    // Refresh peserta dari database setelah berhasil disimpan
                    loadPesertaFromJadwal(jadwalId)
                } else {
                    Log.e("JadwalViewModel", "❌ Gagal assign peserta ke jadwal: ${resp.code()} - ${resp.message()}")
                    resp.errorBody()?.string()?.let { 
                        Log.e("JadwalViewModel", "Error body: $it") 
                    }
                }
            } catch (e: Exception) {
                Log.e("JadwalViewModel", "❌ Error saat assign peserta ke jadwal", e)
            }
        }
    }
    
    fun loadPesertaFromJadwal(jadwalId: Int) {
        if (authToken.isBlank()) {
            Log.w("JadwalViewModel", "AuthToken kosong, tidak bisa load peserta dari jadwal")
            return
        }
        
        viewModelScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    api.getPesertaByJadwal("Bearer $authToken", jadwalId)
                }
                
                if (resp.isSuccessful) {
                    val body = resp.body()
                    body?.data?.let { pesertaList ->
                        try {
                            // Convert PendaftarResponse ke Peserta
                            val peserta = pesertaList.mapNotNull { pendaftar ->
                                try {
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
                                        formulirPendaftaran = pendaftar.formulirPendaftaran?.toString(),
                                        suratKomitmen = pendaftar.suratKomitmen?.toString(),
                                        lulusBerkas = true, // Peserta di jadwal sudah lulus berkas
                                        ditolak = false,
                                        statusSeleksiBerkas = "lulus", // Default lulus karena sudah di jadwal
                                        statusWawancara = pendaftar.statusWawancara ?: "pending",
                                        tanggalJadwal = pendaftar.tanggalJadwal
                                    )
                                } catch (e: Exception) {
                                    Log.e("JadwalViewModel", "Error converting peserta ${pendaftar.id}: ${e.message}", e)
                                    null
                                }
                            }
                            
                            // Hapus peserta yang sama dari jadwal lain untuk mencegah duplikasi
                            val pesertaIds = peserta.mapNotNull { it.id }.toSet()
                            _pesertaPerJadwal.forEach { (otherJadwalId, otherPesertaList) ->
                                if (otherJadwalId != jadwalId) {
                                    // Hapus peserta yang memiliki ID yang sama
                                    otherPesertaList.removeAll { it.id in pesertaIds }
                                }
                            }
                            
                            _pesertaPerJadwal[jadwalId] = peserta.toMutableList()
                            // Trigger recomposition
                            _pesertaPerJadwalUpdateTrigger.value++
                            Log.d("JadwalViewModel", "Berhasil load ${peserta.size} peserta dari jadwal $jadwalId")
                        } catch (e: Exception) {
                            Log.e("JadwalViewModel", "Error processing peserta list: ${e.message}", e)
                        }
                    } ?: run {
                        Log.w("JadwalViewModel", "Response body data is null untuk jadwal $jadwalId")
                    }
                } else {
                    val errorBody = resp.errorBody()?.string()
                    Log.e("JadwalViewModel", "Gagal load peserta dari jadwal $jadwalId: ${resp.code()} - ${resp.message()}. Error body: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("JadwalViewModel", "Error saat load peserta dari jadwal $jadwalId: ${e.message}", e)
            }
        }
    }

    fun setAuthToken(token: String) {
        // Hanya fetch jika token berubah atau jadwal masih kosong
        val tokenChanged = authToken != token
        authToken = token
        
        // Hanya fetch jika token berubah atau daftar jadwal masih kosong
        if (tokenChanged || _daftarJadwal.isEmpty()) {
            fetchJadwal()
            // Load peserta untuk semua jadwal setelah fetch jadwal selesai
            viewModelScope.launch {
                delay(500) // Tunggu fetchJadwal selesai
                _daftarJadwal.forEach { jadwal ->
                    loadPesertaFromJadwal(jadwal.id)
                }
            }
        } else {
            // Jika token tidak berubah tapi peserta belum di-load, load sekarang
            viewModelScope.launch {
                _daftarJadwal.forEach { jadwal ->
                    if (_pesertaPerJadwal[jadwal.id].isNullOrEmpty()) {
                        loadPesertaFromJadwal(jadwal.id)
                    }
                }
            }
        }
    }

    private fun mapRemoteToLocal(item: JadwalRekrutmenItem): Jadwal {
        return Jadwal(
            id = item.id,
            judul = item.judul,
            tanggalMulai = item.tanggalMulai,
            tanggalSelesai = item.tanggalSelesai,
            waktuMulai = item.waktuMulai,
            waktuSelesai = item.waktuSelesai,
            pewawancara = item.pewawancara ?: "-"
        )
    }

    fun fetchJadwal() {
        if (authToken.isBlank()) {
            Log.w("JadwalViewModel", "AuthToken kosong, tidak bisa fetch jadwal")
            return
        }
        viewModelScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    api.getJadwalRekrutmen("Bearer $authToken")
                }
                if (resp.isSuccessful) {
                    val body = resp.body()
                    body?.data?.let { list ->
                        _daftarJadwal.clear()
                        _daftarJadwal.addAll(list.map { mapRemoteToLocal(it) })
                        Log.d("JadwalViewModel", "Berhasil fetch ${list.size} jadwal dari database")
                    } ?: Log.w("JadwalViewModel", "Response body kosong")
                } else {
                    Log.e("JadwalViewModel", "Gagal fetch jadwal: ${resp.code()} - ${resp.message()}")
                    resp.errorBody()?.string()?.let { Log.e("JadwalViewModel", "Error body: $it") }
                }
            } catch (e: Exception) {
                Log.e("JadwalViewModel", "Error saat fetch jadwal", e)
            }
        }
    }

    fun tambahJadwal(
        judul: String,
        tglMulai: String,
        tglSelesai: String,
        jamMulai: String,
        jamSelesai: String,
        pewawancara: String
    ) {
        val newItem = Jadwal(
            id = (_daftarJadwal.maxOfOrNull { it.id } ?: 0) + 1,
            judul = judul.trim(),
            tanggalMulai = tglMulai,
            tanggalSelesai = tglSelesai,
            waktuMulai = jamMulai.trim(),
            waktuSelesai = jamSelesai.trim(),
            pewawancara = pewawancara.trim().ifBlank { "-" }
        )
        _daftarJadwal.add(newItem)

        if (authToken.isBlank()) {
            Log.w("JadwalViewModel", "AuthToken kosong, jadwal hanya tersimpan lokal")
            return
        }
        
        viewModelScope.launch {
            try {
                val requestItem = JadwalRekrutmenItem(
                    id = 0,
                    judul = newItem.judul,
                    tanggalMulai = newItem.tanggalMulai,
                    tanggalSelesai = newItem.tanggalSelesai,
                    waktuMulai = newItem.waktuMulai,
                    waktuSelesai = newItem.waktuSelesai,
                    pewawancara = newItem.pewawancara
                )
                
                Log.d("JadwalViewModel", "Mengirim jadwal ke database: $requestItem")
                
                val resp = withContext(Dispatchers.IO) {
                    api.createJadwalRekrutmen("Bearer $authToken", requestItem)
                }
                
                if (resp.isSuccessful) {
                    val body = resp.body()
                    Log.d("JadwalViewModel", "Jadwal berhasil disimpan ke database: ${body?.data}")
                    // Refresh data dari database untuk mendapatkan ID yang benar
                    fetchJadwal()
                } else {
                    Log.e("JadwalViewModel", "Gagal menyimpan jadwal: ${resp.code()} - ${resp.message()}")
                    resp.errorBody()?.string()?.let { 
                        Log.e("JadwalViewModel", "Error body: $it") 
                    }
                }
            } catch (e: Exception) {
                Log.e("JadwalViewModel", "Error saat menyimpan jadwal ke database", e)
            }
        }
    }

    fun getJadwalById(id: Int) = _daftarJadwal.find { it.id == id }

    fun ubahJadwal(
        id: Int,
        judul: String,
        tglMulai: String,
        tglSelesai: String,
        jamMulai: String,
        jamSelesai: String,
        pewawancara: String
    ) {
        val index = _daftarJadwal.indexOfFirst { it.id == id }
        if (index != -1) {
            val updated = _daftarJadwal[index].copy(
                judul = judul.trim(),
                tanggalMulai = tglMulai,
                tanggalSelesai = tglSelesai,
                waktuMulai = jamMulai.trim(),
                waktuSelesai = jamSelesai.trim(),
                pewawancara = pewawancara.trim().ifBlank { "-" }
            )
            _daftarJadwal[index] = updated

            if (authToken.isBlank()) {
                Log.w("JadwalViewModel", "AuthToken kosong, perubahan hanya tersimpan lokal")
                return
            }
            
            viewModelScope.launch {
                try {
                    val requestItem = JadwalRekrutmenItem(
                        id = id,
                        judul = updated.judul,
                        tanggalMulai = updated.tanggalMulai,
                        tanggalSelesai = updated.tanggalSelesai,
                        waktuMulai = updated.waktuMulai,
                        waktuSelesai = updated.waktuSelesai,
                        pewawancara = updated.pewawancara
                    )
                    
                    Log.d("JadwalViewModel", "Mengupdate jadwal ID $id ke database")
                    
                    val resp = withContext(Dispatchers.IO) {
                        api.updateJadwalRekrutmen("Bearer $authToken", id, requestItem)
                    }
                    
                    if (resp.isSuccessful) {
                        Log.d("JadwalViewModel", "Jadwal berhasil diupdate di database")
                        fetchJadwal()
                    } else {
                        Log.e("JadwalViewModel", "Gagal update jadwal: ${resp.code()} - ${resp.message()}")
                        resp.errorBody()?.string()?.let { 
                            Log.e("JadwalViewModel", "Error body: $it") 
                        }
                    }
                } catch (e: Exception) {
                    Log.e("JadwalViewModel", "Error saat update jadwal ke database", e)
                }
            }
        }
    }

    fun hapusJadwal(id: Int) {
        _daftarJadwal.removeAll { it.id == id }
        if (authToken.isBlank()) {
            Log.w("JadwalViewModel", "AuthToken kosong, penghapusan hanya lokal")
            return
        }
        viewModelScope.launch {
            try {
                Log.d("JadwalViewModel", "Menghapus jadwal ID $id dari database")
                val resp = withContext(Dispatchers.IO) {
                    api.deleteJadwalRekrutmen("Bearer $authToken", id)
                }
                if (resp.isSuccessful) {
                    Log.d("JadwalViewModel", "Jadwal berhasil dihapus dari database")
                    fetchJadwal()
                } else {
                    Log.e("JadwalViewModel", "Gagal hapus jadwal: ${resp.code()} - ${resp.message()}")
                }
            } catch (e: Exception) {
                Log.e("JadwalViewModel", "Error saat hapus jadwal dari database", e)
            }
        }
    }
}
