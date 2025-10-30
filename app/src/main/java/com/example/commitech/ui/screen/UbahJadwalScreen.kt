package com.example.commitech.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.commitech.ui.viewmodel.JadwalViewModel
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UbahJadwalScreen(
    navController: NavController,
    jadwalId: Int,
    viewModel: JadwalViewModel
) {
    val jadwal = viewModel.getJadwalById(jadwalId)
    var judul by remember { mutableStateOf(jadwal?.judul ?: "") }
    var tglMulai by remember { mutableStateOf(jadwal?.tanggalMulai ?: "") }
    var tglSelesai by remember { mutableStateOf(jadwal?.tanggalSelesai ?: "") }
    var wktMulai by remember { mutableStateOf(jadwal?.waktuMulai ?: "") }
    var wktSelesai by remember { mutableStateOf(jadwal?.waktuSelesai ?: "") }
    var tanggalMulaiDialog by remember { mutableStateOf(false) }
    var tanggalSelesaiDialog by remember { mutableStateOf(false) }
    var waktuMulaiDialog by remember { mutableStateOf(false) }
    var waktuSelesaiDialog by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("in", "ID"))

    Column(Modifier.padding(16.dp)) {
        Text(
            "Ubah Jadwal",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = judul,
            onValueChange = { judul = it },
            label = { Text("Judul Kegiatan") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Tanggal Mulai", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { tanggalMulaiDialog = true }
                ) {
                    Text(if (tglMulai.isBlank()) "Pilih Tanggal" else tglMulai)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Tanggal Selesai", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { tanggalSelesaiDialog = true }
                ) {
                    Text(if (tglSelesai.isBlank()) "Pilih Tanggal" else tglSelesai)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Waktu Mulai", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { waktuMulaiDialog = true }
                ) {
                    Text(if (wktMulai.isBlank()) "Pilih Waktu" else wktMulai)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Waktu Selesai", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { waktuSelesaiDialog = true }
                ) {
                    Text(if (wktSelesai.isBlank()) "Pilih Waktu" else wktSelesai)
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                if (judul.isNotBlank() && tglMulai.isNotBlank() && tglSelesai.isNotBlank()) {
                    viewModel.ubahJadwal(
                        jadwalId,
                        judul,
                        tglMulai,
                        tglSelesai,
                        wktMulai.ifBlank { "-" },
                        wktSelesai.ifBlank { "-" }
                    )
                    // 🔹 Navigasi langsung ke halaman jadwal rekrutmen setelah simpan
                    navController.navigate("jadwalRekrutmen") {
                        popUpTo("jadwalRekrutmen") { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan Perubahan")
        }
    }

    // ================= DATE PICKERS =================
    if (tanggalMulaiDialog) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { tanggalMulaiDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        val localDate = LocalDate.ofEpochDay(it / 86_400_000L)
                        tglMulai = localDate.format(dateFormatter)
                    }
                    tanggalMulaiDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { tanggalMulaiDialog = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (tanggalSelesaiDialog) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { tanggalSelesaiDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        val localDate = LocalDate.ofEpochDay(it / 86_400_000L)
                        tglSelesai = localDate.format(dateFormatter)
                    }
                    tanggalSelesaiDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { tanggalSelesaiDialog = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    // ================= TIME PICKERS =================
    if (waktuMulaiDialog) {
        val timeState = rememberTimePickerState(initialHour = 9, initialMinute = 0)
        AlertDialog(
            onDismissRequest = { waktuMulaiDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val jam = String.format("%02d", timeState.hour)
                    val menit = String.format("%02d", timeState.minute)
                    wktMulai = "$jam.$menit"
                    waktuMulaiDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { waktuMulaiDialog = false }) { Text("Batal") }
            },
            title = { Text("Pilih Waktu Mulai") },
            text = { TimeInput(state = timeState) }
        )
    }

    if (waktuSelesaiDialog) {
        val timeState = rememberTimePickerState(initialHour = 15, initialMinute = 0)
        AlertDialog(
            onDismissRequest = { waktuSelesaiDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val jam = String.format("%02d", timeState.hour)
                    val menit = String.format("%02d", timeState.minute)
                    wktSelesai = "$jam.$menit"
                    waktuSelesaiDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { waktuSelesaiDialog = false }) { Text("Batal") }
            },
            title = { Text("Pilih Waktu Selesai") },
            text = { TimeInput(state = timeState) }
        )
    }
}
