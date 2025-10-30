package com.example.commitech.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

// === Data Model Jadwal ===
data class Jadwal(
    val id: Int,
    val judul: String,
    val tanggalMulai: String,
    val tanggalSelesai: String,
    val waktuMulai: String,
    val waktuSelesai: String
)

// === ViewModel Jadwal ===
class JadwalViewModel : ViewModel() {

    // 🔹 Formatter global agar seragam di semua layar
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("in", "ID"))

    // 🔹 Auto ID untuk setiap jadwal baru
    private var nextId = 3

    // 🔹 Data jadwal disimpan di state agar UI otomatis update
    var daftarJadwal = mutableStateListOf(
        Jadwal(
            id = 1,
            judul = "Seleksi Berkas",
            tanggalMulai = "23 Okt 2025",
            tanggalSelesai = "25 Okt 2025",
            waktuMulai = "09.00",
            waktuSelesai = "15.00"
        ),
        Jadwal(
            id = 2,
            judul = "Seleksi Wawancara",
            tanggalMulai = "26 Okt 2025",
            tanggalSelesai = "28 Okt 2025",
            waktuMulai = "09.00",
            waktuSelesai = "15.00"
        )
    )
        private set

    // ✅ Tambah Jadwal Baru
    fun tambahJadwal(
        judul: String,
        tanggalMulai: String,
        tanggalSelesai: String,
        waktuMulai: String,
        waktuSelesai: String
    ) {
        daftarJadwal.add(
            Jadwal(
                id = nextId++,
                judul = judul,
                tanggalMulai = tanggalMulai,
                tanggalSelesai = tanggalSelesai,
                waktuMulai = waktuMulai,
                waktuSelesai = waktuSelesai
            )
        )
    }

    // ✅ Ambil Jadwal Berdasarkan ID
    fun getJadwalById(id: Int): Jadwal? = daftarJadwal.find { it.id == id }

    // ✅ Ubah Jadwal Berdasarkan ID
    fun ubahJadwal(
        id: Int,
        judul: String,
        tanggalMulai: String,
        tanggalSelesai: String,
        waktuMulai: String,
        waktuSelesai: String
    ) {
        val index = daftarJadwal.indexOfFirst { it.id == id }
        if (index != -1) {
            daftarJadwal[index] = Jadwal(
                id,
                judul,
                tanggalMulai,
                tanggalSelesai,
                waktuMulai,
                waktuSelesai
            )
        }
    }
}
