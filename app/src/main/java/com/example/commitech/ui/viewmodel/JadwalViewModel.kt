package com.example.commitech.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Jadwal(
    val id: Int,
    val judul: String,
    val tanggalMulai: String,
    val tanggalSelesai: String,
    val waktuMulai: String,
    val waktuSelesai: String
)

class JadwalViewModel : ViewModel() {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("in", "ID"))
    private var nextId = 3
    private val _daftarJadwal = mutableStateListOf(
        Jadwal(1, "Seleksi Berkas", "23 Okt 2025", "25 Okt 2025", "09.00", "15.00"),
        Jadwal(2, "Seleksi Wawancara", "26 Okt 2025", "28 Okt 2025", "09.00", "15.00")
    )
    val daftarJadwal: List<Jadwal> get() = _daftarJadwal

    fun tambahJadwal(judul: String, tglMulai: String, tglSelesai: String, jamMulai: String, jamSelesai: String) {
        _daftarJadwal.add(Jadwal(nextId++, judul.trim(), tglMulai, tglSelesai, jamMulai.trim(), jamSelesai.trim()))
    }

    fun getJadwalById(id: Int) = _daftarJadwal.find { it.id == id }

    fun ubahJadwal(id: Int, judul: String, tglMulai: String, tglSelesai: String, jamMulai: String, jamSelesai: String) {
        val index = _daftarJadwal.indexOfFirst { it.id == id }
        if (index != -1) {
            _daftarJadwal[index] = _daftarJadwal[index].copy(
                judul = judul.trim(),
                tanggalMulai = tglMulai,
                tanggalSelesai = tglSelesai,
                waktuMulai = jamMulai.trim(),
                waktuSelesai = jamSelesai.trim()
            )
        }
    }

    fun hapusJadwal(id: Int) {
        _daftarJadwal.removeAll { it.id == id }
    }
}
