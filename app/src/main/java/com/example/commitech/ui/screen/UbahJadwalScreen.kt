package com.example.commitech.ui.screens.jadwal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.commitech.ui.viewmodel.JadwalViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UbahJadwalScreen(
    navController: NavController,
    viewModel: JadwalViewModel,
    jadwalId: Int
) {
    val jadwal = viewModel.getJadwalById(jadwalId) ?: return
    
    var judul by remember { mutableStateOf(jadwal.judul) }
    var tglMulai by remember { mutableStateOf(jadwal.tanggalMulai) }
    var tglSelesai by remember { mutableStateOf(jadwal.tanggalSelesai) }
    var wktMulai by remember { mutableStateOf(jadwal.waktuMulai) }
    var wktSelesai by remember { mutableStateOf(jadwal.waktuSelesai) }

    var tanggalMulaiDialog by remember { mutableStateOf(false) }
    var tanggalSelesaiDialog by remember { mutableStateOf(false) }
    var waktuMulaiDialog by remember { mutableStateOf(false) }
    var waktuSelesaiDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("in", "ID"))
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Ubah Jadwal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .background(Color(0xFFFFEBEE), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        containerColor = colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(16.dp))
            
            // Card untuk form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        "Informasi Jadwal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorScheme.onSurface
                    )
                    
                    Spacer(Modifier.height(20.dp))
                    
                    OutlinedTextField(
                        value = judul,
                        onValueChange = { judul = it },
                        label = { Text("Judul Jadwal") },
                        placeholder = { Text("Masukkan judul jadwal...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            focusedLabelColor = colorScheme.primary
                        )
                    )

                    Spacer(Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Tanggal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colorScheme.onSurface
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))

                    // Baris tanggal mulai & selesai
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(
                            onClick = { tanggalMulaiDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.onSurface
                            )
                        ) {
                            Text(
                                if (tglMulai.isBlank()) "Tanggal Mulai" else tglMulai,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = { tanggalSelesaiDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.onSurface
                            )
                        ) {
                            Text(
                                if (tglSelesai.isBlank()) "Tanggal Selesai" else tglSelesai,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Waktu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colorScheme.onSurface
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))

                    // Baris waktu mulai & selesai
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { waktuMulaiDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.onSurface
                            )
                        ) {
                            Text(
                                if (wktMulai.isBlank()) "09.00" else wktMulai,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "→",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = { waktuSelesaiDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.onSurface
                            )
                        ) {
                            Text(
                                if (wktSelesai.isBlank()) "15.00" else wktSelesai,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Tombol simpan
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
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary
                )
            ) {
                Text(
                    "Simpan Perubahan",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(Modifier.height(20.dp))

            // DatePicker dialogs
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

            // TimePicker dialogs
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

            // Delete confirmation dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Hapus Jadwal") },
                    text = { Text("Apakah Anda yakin ingin menghapus jadwal ini?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.hapusJadwal(jadwalId)
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFFE91E63)
                            )
                        ) {
                            Text("Hapus")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}
