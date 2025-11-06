package com.example.commitech.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.commitech.ui.viewmodel.JadwalViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifikasiScreen(navController: NavController, viewModel: JadwalViewModel) {
    val jadwalList = viewModel.daftarJadwal
    val dateFormatter = viewModel.formatter
    val hariIni = LocalDate.now()

    val jadwalDekat = jadwalList.filter {
        try {
            val mulai = LocalDate.parse(it.tanggalMulai, dateFormatter)
            ChronoUnit.DAYS.between(hariIni, mulai) in 0..1
        } catch (e: Exception) {
            false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifikasi Jadwal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (jadwalDekat.isEmpty()) {
                Text("Tidak ada jadwal wawancara yang dekat.", style = MaterialTheme.typography.bodyLarge)
            } else {
                Text("Jadwal wawancara terdekat:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                jadwalDekat.forEach { jadwal ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(jadwal.judul, style = MaterialTheme.typography.titleMedium)
                            Text("Tanggal: ${jadwal.tanggalMulai} - ${jadwal.tanggalSelesai}")
                            Text("Waktu: ${jadwal.waktuMulai} - ${jadwal.waktuSelesai}")
                        }
                    }
                }
            }
        }
    }
}
