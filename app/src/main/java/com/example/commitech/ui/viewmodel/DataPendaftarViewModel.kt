package com.example.commitech.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// MODIFIKASI: Tambahkan properti untuk detail yang bisa di-edit
data class Pendaftar(
    val id: Int,
    val nama: String,
    val nim: String,
    val divisi1: String, // BARU
    val alasan1: String, // BARU
    val divisi2: String, // BARU
    val alasan2: String // BARU
)

class DataPendaftarViewModel : ViewModel() {

    // MODIFIKASI: Tambahkan data dummy untuk properti baru
    private val _pendaftarList = MutableStateFlow(listOf(
        Pendaftar(1, "Fadhilla Firma", "2311522031", "Konsumsi", "Suka masak", "Acara", "Suka keramaian"),
        Pendaftar(2, "Afiq Congkel", "2311523011", "Humas", "Suka komunikasi", "Pubdok", "Suka foto"),
        Pendaftar(3, "Farhan Firki", "2311522037", "Perlengkapan", "Suka angkat-angkat", "Keamanan", "Suka ketertiban"),
        Pendaftar(4, "Diaz Jelek", "2311521015", "Acara", "Suka buat event", "Konsumsi", "Suka ngemil"),
        Pendaftar(5, "Fadhilla Firma", "2311522031", "Konsumsi", "Suka masak", "Acara", "Suka keramaian"),
        Pendaftar(6, "Afiq Congkel", "2311523011", "Humas", "Suka komunikasi", "Pubdok", "Suka foto"),
        Pendaftar(7, "Farhan Firki", "2311522037", "Perlengkapan", "Suka angkat-angkat", "Keamanan", "Suka ketertiban"),
        Pendaftar(8, "Diaz Jelek", "2311521015", "Acara", "Suka buat event", "Konsumsi", "Suka ngemil"),
        Pendaftar(9, "Fadhilla Firma", "2311522031", "Konsumsi", "Suka masak", "Acara", "Suka keramaian"),
        Pendaftar(10, "Afiq Congkel", "2311523011", "Humas", "Suka komunikasi", "Pubdok", "Suka foto"),
        Pendaftar(11, "Farhan Firki", "2311522037", "Perlengkapan", "Suka angkat-angkat", "Keamanan", "Suka ketertiban"),
        Pendaftar(12, "Diaz Jelek", "2311521015", "Acara", "Suka buat event", "Konsumsi", "Suka ngemil"),
        Pendaftar(13, "Fadhilla Firma", "2311522031", "Konsumsi", "Suka masak", "Acara", "Suka keramaian"),
        Pendaftar(14, "Afiq Congkel", "2311523011", "Humas", "Suka komunikasi", "Pubdok", "Suka foto"),
        Pendaftar(15, "Farhan Firki", "2311522037", "Perlengkapan", "Suka angkat-angkat", "Keamanan", "Suka ketertiban")
    ))

    val pendaftarList: StateFlow<List<Pendaftar>> = _pendaftarList.asStateFlow()

    fun deletePendaftar(pendaftar: Pendaftar) {
        _pendaftarList.update { listSaatIni ->
            listSaatIni.filterNot { it.id == pendaftar.id }
        }
    }

    // FUNGSI BARU: Untuk menyimpan data yang diedit
    fun editPendaftar(pendaftarBaru: Pendaftar) {
        _pendaftarList.update { listSaatIni ->
            listSaatIni.map { pendaftarLama ->
                if (pendaftarLama.id == pendaftarBaru.id) {
                    pendaftarBaru // Ganti dengan data baru
                } else {
                    pendaftarLama
                }
            }
        }
    }

    // Fungsi updatePendaftar lama hanya digunakan untuk memicu dialog, tidak perlu diubah.
    fun updatePendaftar(pendaftar: Pendaftar) {
        // Fungsi ini sekarang hanya berfungsi sebagai pemicu (trigger) untuk membuka Edit Dialog
        println("TRIGGER: Membuka dialog edit untuk ${pendaftar.nama}")
    }
}