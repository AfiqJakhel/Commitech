package com.example.commitech.ui.viewmodel

import androidx.lifecycle.ViewModel

data class Pendaftar(
    val id: Int,
    val nama: String,
    val nim: String
)

class DataPendaftarViewModel : ViewModel() {
    val dummyPendaftarList = listOf(
        Pendaftar(1, "Fadhilla Firma", "2311522031"),
        Pendaftar(2, "Afiq Congkel", "2311523011"),
        Pendaftar(3, "Farhan Firki", "2311522037"),
        Pendaftar(4, "Diaz Jelek", "2311521015"),
        Pendaftar(5, "Fadhilla Firma", "2311522031"),
        Pendaftar(6, "Afiq Congkel", "2311523011"),
        Pendaftar(7, "Farhan Firki", "2311522037"),
        Pendaftar(8, "Diaz Jelek", "2311521015"),
        Pendaftar(9, "Fadhilla Firma", "2311522031"),
        Pendaftar(10, "Afiq Congkel", "2311523011"),
        Pendaftar(11, "Farhan Firki", "2311522037"),
        Pendaftar(12, "Diaz Jelek", "2311521015"),
        Pendaftar(13, "Fadhilla Firma", "2311522031"),
        Pendaftar(14, "Afiq Congkel", "2311523011"),
        Pendaftar(15, "Farhan Firki", "2311522037")
    )
}
