package com.example.commitech.ui.screens.jadwal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.commitech.ui.viewmodel.JadwalViewModel
import java.time.LocalDate
import androidx.compose.ui.unit.dp


@Composable
fun UbahJadwalScreen(
    navController: NavController,
    viewModel: JadwalViewModel,
    jadwalId: Int
) {
    val jadwal = viewModel.getJadwalById(jadwalId) ?: return
    var judul by remember { mutableStateOf(jadwal.judul) }
    var tanggalMulai by remember { mutableStateOf(LocalDate.parse(jadwal.tanggalMulai, viewModel.formatter)) }
    var tanggalSelesai by remember { mutableStateOf(LocalDate.parse(jadwal.tanggalSelesai, viewModel.formatter)) }
    var waktuMulai by remember { mutableStateOf(jadwal.waktuMulai) }
    var waktuSelesai by remember { mutableStateOf(jadwal.waktuSelesai) }

    val dateFormatter = viewModel.formatter

    Column(Modifier.padding(16.dp)) {
        Text("Ubah Jadwal", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(judul, { judul = it }, label = { Text("Judul Jadwal") })
        Spacer(Modifier.height(8.dp))
        Text("Tanggal Mulai: ${tanggalMulai.format(dateFormatter)}")
        Text("Tanggal Selesai: ${tanggalSelesai.format(dateFormatter)}")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(waktuMulai, { waktuMulai = it }, label = { Text("Waktu Mulai") })
        OutlinedTextField(waktuSelesai, { waktuSelesai = it }, label = { Text("Waktu Selesai") })
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            viewModel.ubahJadwal(
                jadwalId,
                judul,
                tanggalMulai.format(dateFormatter),
                tanggalSelesai.format(dateFormatter),
                waktuMulai,
                waktuSelesai
            )
            navController.popBackStack()
        }) {
            Text("Simpan Perubahan")
        }
    }
}
