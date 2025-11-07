package com.example.commitech.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

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
