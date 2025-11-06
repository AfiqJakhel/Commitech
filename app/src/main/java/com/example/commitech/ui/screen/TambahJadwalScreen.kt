package com.example.commitech.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.commitech.ui.viewmodel.JadwalViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahJadwalScreen(
    navController: NavController,
    viewModel: JadwalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var judul by remember { mutableStateOf("") }
    var tglMulai by remember { mutableStateOf("") }
    var tglSelesai by remember { mutableStateOf("") }
    var wktMulai by remember { mutableStateOf("") }
    var wktSelesai by remember { mutableStateOf("") }

    var tanggalMulaiDialog by remember { mutableStateOf(false) }
    var tanggalSelesaiDialog by remember { mutableStateOf(false) }
    var waktuMulaiDialog by remember { mutableStateOf(false) }
    var waktuSelesaiDialog by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale("in", "ID"))

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tambah",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFF1A1A40)
                        )
                        Text(
                            text = "Jadwal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFF1A1A40)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF1A1A40)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = judul,
                onValueChange = { judul = it },
                label = { Text("Judul") },
                placeholder = { Text("isi Judul tempat disini...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Text("Waktu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            // 🔹 Baris tanggal mulai & selesai
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = { tanggalMulaiDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (tglMulai.isBlank()) "Pilih Tanggal Mulai" else tglMulai)
                }
                Spacer(Modifier.width(10.dp))
                OutlinedButton(
                    onClick = { tanggalSelesaiDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (tglSelesai.isBlank()) "Pilih Tanggal Selesai" else tglSelesai)
                }
            }

            Spacer(Modifier.height(16.dp))

            // 🔹 Baris waktu mulai & selesai
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { waktuMulaiDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (wktMulai.isBlank()) "09.00" else wktMulai)
                }
                Spacer(Modifier.width(10.dp))
                Text("→", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                OutlinedButton(
                    onClick = { waktuSelesaiDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (wktSelesai.isBlank()) "15.00" else wktSelesai)
                }
            }

            Spacer(Modifier.height(32.dp))

            // 🔹 Tombol simpan
            Button(
                onClick = {
                    if (judul.isNotBlank() && tglMulai.isNotBlank() && tglSelesai.isNotBlank()) {
                        viewModel.tambahJadwal(
                            judul,
                            tglMulai,
                            tglSelesai,
                            wktMulai.ifBlank { "-" },
                            wktSelesai.ifBlank { "-" }
                        )

                        // ✅ Langsung kembali ke halaman sebelumnya (JadwalRekrutmenScreen)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan Jadwal")
            }




            // 🔹 DatePicker dialogs
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
                ) { DatePicker(state = dateState) }
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
                ) { DatePicker(state = dateState) }
            }

            // 🔹 TimePicker dialogs
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
    }
}
