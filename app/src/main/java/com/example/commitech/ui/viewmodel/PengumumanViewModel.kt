package com.example.commitech.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.commitech.data.model.HasilWawancaraRequest
import com.example.commitech.data.repository.HasilWawancaraRepository
import kotlinx.coroutines.launch

enum class InterviewStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class ParticipantInfo(
    val name: String,
    var status: InterviewStatus = InterviewStatus.ACCEPTED,
    var division: String = ""
)

data class DivisiData(
    val namaDivisi: String,
    val koordinator: String,
    val pesertaLulus: androidx.compose.runtime.snapshots.SnapshotStateList<ParticipantInfo>
)

class PengumumanViewModel : ViewModel() {

    // Divisi template yang sudah ada
    private val _daftarDivisi = mutableStateListOf(
        DivisiData("Acara", "Ihsan", mutableStateListOf()),
        DivisiData("Humas", "Anisa", mutableStateListOf()),
        DivisiData("Konsumsi", "Budi", mutableStateListOf()),
        DivisiData("Perlengkapan", "Siti", mutableStateListOf())
    )

    val daftarDivisi: List<DivisiData> get() = _daftarDivisi
    
    private val hasilWawancaraRepository = HasilWawancaraRepository()

    fun getKoordinator(divisi: String): String {
        return _daftarDivisi.find { it.namaDivisi == divisi }?.koordinator ?: "-"
    }

    fun getAllParticipants(): List<ParticipantInfo> {
        return _daftarDivisi.flatMap { it.pesertaLulus }
    }

    fun getAllDivisiNames(): List<String> = _daftarDivisi.map { it.namaDivisi }

    // Sinkronisasi data dari SeleksiWawancaraViewModel
    fun syncFromSeleksiWawancara(seleksiViewModel: SeleksiWawancaraViewModel) {
        // Hapus semua peserta yang ada
        _daftarDivisi.forEach { it.pesertaLulus.clear() }
        
        // Ambil peserta yang diterima dari seleksi wawancara
        val acceptedParticipants = seleksiViewModel.getAllParticipants()
            .filter { it.status == InterviewStatus.ACCEPTED && it.division.isNotBlank() }
        
        // Tambahkan ke divisi yang sesuai
        acceptedParticipants.forEach { participant ->
            val targetDivisi = _daftarDivisi.find { it.namaDivisi == participant.division }
            targetDivisi?.pesertaLulus?.add(
                ParticipantInfo(
                    name = participant.name,
                    status = InterviewStatus.ACCEPTED,
                    division = participant.division
                )
            )
        }
    }

    fun rejectParticipantByName(name: String) {
        _daftarDivisi.forEach { div ->
            div.pesertaLulus.removeIf { it.name == name }
        }
    }

    fun updateParticipantDivisionAndStatus(
        name: String, 
        divisi: String, 
        status: InterviewStatus,
        seleksiViewModel: SeleksiWawancaraViewModel,
        token: String? = null,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        // Cari peserta di SeleksiWawancaraViewModel untuk mendapatkan pesertaId
        val participant = seleksiViewModel.getAllParticipants().find { it.name == name }
        if (participant?.pesertaId == null) {
            onError?.invoke("Peserta tidak ditemukan atau pesertaId tidak tersedia")
            return
        }
        
        val pesertaId = participant.pesertaId!!
        
        // Jika token tersedia, update ke backend terlebih dahulu
        if (token != null) {
            // Cari hasil wawancara yang sudah ada untuk mendapatkan hasilWawancaraId
            val hasilWawancara = seleksiViewModel.getHasilWawancaraByPesertaId(pesertaId)
            if (hasilWawancara != null) {
                // Update ke backend
                viewModelScope.launch {
                    try {
                        val request = HasilWawancaraRequest(
                            pesertaId = pesertaId,
                            status = when (status) {
                                InterviewStatus.ACCEPTED -> "diterima"
                                InterviewStatus.REJECTED -> "ditolak"
                                InterviewStatus.PENDING -> "pending"
                            },
                            divisi = if (status == InterviewStatus.ACCEPTED) divisi else null,
                            alasan = if (status == InterviewStatus.REJECTED) "Diubah menjadi ditolak" else null
                        )
                        
                        val response = hasilWawancaraRepository.ubahHasilWawancara(
                            token = token,
                            id = hasilWawancara.id,
                            request = request
                        )
                        
                        if (response.isSuccessful && response.body() != null) {
                            val responseBody = response.body()!!
                            if (responseBody.sukses && responseBody.data != null) {
                                // Refresh data dari database untuk memastikan sinkronisasi
                                seleksiViewModel.loadHasilWawancaraAndUpdateStatus(token)
                                
                                // Update local state setelah berhasil update ke backend
                                updateLocalState(name, divisi, status, seleksiViewModel)
                                onSuccess?.invoke()
                            } else {
                                onError?.invoke(responseBody.pesan ?: "Gagal mengubah hasil wawancara")
                            }
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                            onError?.invoke("Gagal mengubah hasil wawancara: ${response.code()} - $errorBody")
                        }
                    } catch (e: Exception) {
                        onError?.invoke("Error: ${e.message}")
                    }
                }
            } else {
                // Jika belum ada hasil wawancara, buat baru
                onError?.invoke("Hasil wawancara untuk peserta ini belum ada. Silakan terima peserta terlebih dahulu.")
            }
        } else {
            // Jika token tidak tersedia, hanya update local state
            updateLocalState(name, divisi, status, seleksiViewModel)
            onSuccess?.invoke()
        }
    }
    
    private fun updateLocalState(
        name: String,
        divisi: String,
        status: InterviewStatus,
        seleksiViewModel: SeleksiWawancaraViewModel
    ) {
        // Update di SeleksiWawancaraViewModel terlebih dahulu
        seleksiViewModel.updateParticipantByName(name, status, divisi)
        
        // Jika ditolak, hapus dari semua divisi
        if (status == InterviewStatus.REJECTED) {
            rejectParticipantByName(name)
            return
        }
        
        // Hapus dari divisi lama
        val oldParticipant = getAllParticipants().find { it.name == name }
        if (oldParticipant != null) {
            rejectParticipantByName(name)
        }
        
        // Tambahkan ke divisi baru
        val targetDivisi = _daftarDivisi.find { it.namaDivisi == divisi }
        if (targetDivisi != null) {
            targetDivisi.pesertaLulus.add(
                ParticipantInfo(
                    name = name,
                    status = status,
                    division = divisi
                )
            )
        }
    }

    fun moveParticipantToDivision(name: String, newDivision: String) {
        // Hapus peserta dari divisi lama
        val peserta = getAllParticipants().find { it.name == name } ?: return
        rejectParticipantByName(name)

        // Tambahkan ke divisi baru
        val targetDivisi = _daftarDivisi.find { it.namaDivisi == newDivision }
        if (targetDivisi != null) {
            targetDivisi.pesertaLulus.add(
                ParticipantInfo(
                    name = name,
                    status = InterviewStatus.ACCEPTED,
                    division = newDivision
                )
            )
        }
    }

}
