package com.example.commitech.ui.viewmodel

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
    val pesertaLulus: MutableList<ParticipantInfo>
)

class PengumumanViewModel : ViewModel() {

    private val _daftarDivisi = mutableListOf(
        DivisiData(
            "Acara", "Ihsan",
            mutableListOf(
                ParticipantInfo("Fadhilla Firma", InterviewStatus.ACCEPTED, "Acara"),
                ParticipantInfo("Afiq Congkel", InterviewStatus.ACCEPTED, "Acara")
            )
        ),
        DivisiData(
            "Humas", "Anisa",
            mutableListOf(
                ParticipantInfo("Putri Ramadhani", InterviewStatus.ACCEPTED, "Humas"),
                ParticipantInfo("Rafi Rizky", InterviewStatus.ACCEPTED, "Humas")
            )
        )
    )

    val daftarDivisi: List<DivisiData> get() = _daftarDivisi

    fun getKoordinator(divisi: String): String {
        return _daftarDivisi.find { it.namaDivisi == divisi }?.koordinator ?: "-"
    }

    fun getAllParticipants(): List<ParticipantInfo> {
        return _daftarDivisi.flatMap { it.pesertaLulus }
    }

    fun getAllDivisiNames(): List<String> = _daftarDivisi.map { it.namaDivisi }

    fun rejectParticipantByName(name: String) {
        _daftarDivisi.forEach { div ->
            div.pesertaLulus.removeIf { it.name == name }
        }
    }

    fun updateParticipantDivisionAndStatus(name: String, divisi: String, status: InterviewStatus) {
        _daftarDivisi.forEach { div ->
            val participant = div.pesertaLulus.find { it.name == name }
            if (participant != null) {
                participant.division = divisi
                participant.status = status
            }
        }

        if (status == InterviewStatus.REJECTED) {
            rejectParticipantByName(name)
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
                peserta.copy(division = newDivision, status = InterviewStatus.ACCEPTED)
            )
        }
    }

}
