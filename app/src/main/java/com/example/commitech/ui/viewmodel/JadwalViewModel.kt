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
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

data class Jadwal(
    val id: Int,
    val judul: String,
    val tanggalMulai: String,
    val tanggalSelesai: String,
    val waktuMulai: String,
    val waktuSelesai: String,
    val pewawancara: String,
    val lokasi: String
)

class JadwalViewModel : ViewModel() {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("in", "ID"))
    private val api: ApiService = RetrofitClient.apiService

    private var authToken: String = ""

    private val _daftarJadwal = mutableStateListOf<Jadwal>()
    val daftarJadwal: List<Jadwal> get() = _daftarJadwal

    private val _pesertaPerJadwal = mutableMapOf<Int, MutableList<Peserta>>()
    val pesertaPerJadwal: Map<Int, List<Peserta>> get() = _pesertaPerJadwal

    private val _pesertaPerJadwalUpdateTrigger = kotlinx.coroutines.flow.MutableStateFlow(0)
    val pesertaPerJadwalUpdateTrigger: kotlinx.coroutines.flow.StateFlow<Int> = _pesertaPerJadwalUpdateTrigger.asStateFlow()
    
    fun getPesertaByJadwalId(jadwalId: Int): List<Peserta> {
        return (_pesertaPerJadwal[jadwalId] ?: emptyList()).toList()
    }

    fun getAllPesertaNamaDiJadwalLain(kecualiJadwalId: Int): Set<String> {
        return _pesertaPerJadwal
            .filterKeys { it != kecualiJadwalId }
            .values
            .flatten()
            .map { it.nama }
            .toSet()
    }

    fun hapusPesertaDariJadwal(jadwalId: Int, peserta: Peserta) {
        val pesertaId = peserta.id

        if (pesertaId != null) {
            _pesertaPerJadwal[jadwalId]?.removeAll { it.id == pesertaId }
        } else {
            _pesertaPerJadwal[jadwalId]?.removeAll { it.nama == peserta.nama }
        }

        _pesertaPerJadwalUpdateTrigger.value++

        if (authToken.isNotBlank() && pesertaId != null) {
            removePesertaFromJadwal(jadwalId, pesertaId)
        } else {
            Log.w(
                "JadwalViewModel",
                "Tidak bisa hapus peserta dari database: authToken=${authToken.isNotBlank()}, pesertaId=$pesertaId"
            )
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
        val pesertaTerbatas = pesertaList.take(5)
        _pesertaPerJadwal[jadwalId] = pesertaTerbatas.toMutableList()
        _pesertaPerJadwalUpdateTrigger.value++

        if (authToken.isNotBlank() && pesertaTerbatas.isNotEmpty()) {
            savePesertaToJadwal(jadwalId, pesertaTerbatas)
        } else if (pesertaTerbatas.isEmpty()) {
            Log.d("JadwalViewModel", "Tidak ada peserta yang dipilih untuk jadwal $jadwalId")
        }
    }

    private fun savePesertaToJadwal(jadwalId: Int, pesertaList: List<Peserta>) {
        viewModelScope.launch {
            try {
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
                                        formulirPendaftaran = pendaftar.formulirPendaftaran,
                                        suratKomitmen = pendaftar.suratKomitmen,
                                        lulusBerkas = true,
                                        ditolak = false,
                                        statusSeleksiBerkas = "lulus",
                                        statusWawancara = pendaftar.statusWawancara ?: "pending",
                                        tanggalJadwal = pendaftar.tanggalJadwal
                                    )
                                } catch (e: Exception) {
                                    Log.e("JadwalViewModel", "Error converting peserta ${pendaftar.id}: ${e.message}", e)
                                    null
                                }
                            }

                            val pesertaIds = peserta.mapNotNull { it.id }.toSet()
                            _pesertaPerJadwal.forEach { (otherJadwalId, otherPesertaList) ->
                                if (otherJadwalId != jadwalId) {
                                    otherPesertaList.removeAll { it.id in pesertaIds }
                                }
                            }
                            
                            _pesertaPerJadwal[jadwalId] = peserta.toMutableList()
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
        val tokenChanged = authToken != token
        authToken = token

        if (tokenChanged || _daftarJadwal.isEmpty()) {
            fetchJadwal()
            viewModelScope.launch {
                delay(500)
                _daftarJadwal.forEach { jadwal ->
                    loadPesertaFromJadwal(jadwal.id)
                }
            }
        } else {
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
            pewawancara = item.pewawancara ?: "-",
            lokasi = item.lokasi ?: ""
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
        pewawancara: String,
        lokasi: String
    ) {
        val newItem = Jadwal(
            id = (_daftarJadwal.maxOfOrNull { it.id } ?: 0) + 1,
            judul = judul.trim(),
            tanggalMulai = tglMulai,
            tanggalSelesai = tglSelesai,
            waktuMulai = jamMulai.trim(),
            waktuSelesai = jamSelesai.trim(),
            pewawancara = pewawancara.trim().ifBlank { "-" },
            lokasi = lokasi.trim()
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
                    pewawancara = newItem.pewawancara,
                    lokasi = newItem.lokasi
                )
                
                Log.d("JadwalViewModel", "Mengirim jadwal ke database: $requestItem")
                
                val resp = withContext(Dispatchers.IO) {
                    api.createJadwalRekrutmen("Bearer $authToken", requestItem)
                }
                
                if (resp.isSuccessful) {
                    val body = resp.body()
                    Log.d("JadwalViewModel", "Jadwal berhasil disimpan ke database: ${body?.data}")
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
        pewawancara: String,
        lokasi: String
    ) {
        val index = _daftarJadwal.indexOfFirst { it.id == id }
        if (index != -1) {
            val updated = _daftarJadwal[index].copy(
                judul = judul.trim(),
                tanggalMulai = tglMulai,
                tanggalSelesai = tglSelesai,
                waktuMulai = jamMulai.trim(),
                waktuSelesai = jamSelesai.trim(),
                pewawancara = pewawancara.trim().ifBlank { "-" },
                lokasi = lokasi.trim()
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
                        pewawancara = updated.pewawancara,
                        lokasi = updated.lokasi
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
