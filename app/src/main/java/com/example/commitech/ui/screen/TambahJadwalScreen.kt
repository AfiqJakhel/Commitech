package com.example.commitech.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.commitech.notification.InterviewAlarmScheduler
import com.example.commitech.notification.InterviewNotificationHelper
import com.example.commitech.ui.viewmodel.JadwalViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahJadwalScreen(
    navController: NavController,
    viewModel: JadwalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    var judul by remember { mutableStateOf("") }
    var pewawancara by remember { mutableStateOf("") }
    var lokasi by remember { mutableStateOf("") }
    var tglMulai by remember { mutableStateOf("") }
    var tglSelesai by remember { mutableStateOf("") }
    var wktMulai by remember { mutableStateOf("") }
    var wktSelesai by remember { mutableStateOf("") }

    var tanggalMulaiDialog by remember { mutableStateOf(false) }
    var tanggalSelesaiDialog by remember { mutableStateOf(false) }
    var waktuMulaiDialog by remember { mutableStateOf(false) }
    var waktuSelesaiDialog by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("in", "ID"))
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Tambah Jadwal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = colorScheme.onBackground
                        )
                    }
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

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pewawancara,
                        onValueChange = { pewawancara = it },
                        label = { Text("Nama Pewawancara") },
                        placeholder = { Text("Masukkan nama pewawancara...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            focusedLabelColor = colorScheme.primary
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = lokasi,
                        onValueChange = { lokasi = it },
                        label = { Text("Lokasi Wawancara") },
                        placeholder = { Text("Masukkan lokasi wawancara...") },
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
                                tglMulai.ifBlank { "Tanggal Mulai" },
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
                                tglSelesai.ifBlank { "Tanggal Selesai" },
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
                                wktMulai.ifBlank { "09.00" },
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
                                wktSelesai.ifBlank { "15.00" },
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = @androidx.annotation.RequiresPermission(android.Manifest.permission.POST_NOTIFICATIONS) {
                    if (judul.isNotBlank() && tglMulai.isNotBlank() && tglSelesai.isNotBlank()) {
                        val jadwalId = viewModel.daftarJadwal.maxOfOrNull { it.id }?.plus(1) ?: 1
                        viewModel.tambahJadwal(
                            judul,
                            tglMulai,
                            tglSelesai,
                            wktMulai.ifBlank { "-" },
                            wktSelesai.ifBlank { "-" },
                            pewawancara,
                            lokasi
                        )
                        
                        try {
                            val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID"))
                            val tanggalJadwal = LocalDate.parse(tglMulai, dateFormatter)
                            val hariIni = LocalDate.now()
                            val daysBetween = ChronoUnit.DAYS.between(hariIni, tanggalJadwal)
                            
                            if (daysBetween == 0L || daysBetween == 1L) {
                                InterviewNotificationHelper.showJadwalUrgentNotification(
                                    context = context,
                                    judulJadwal = judul,
                                    tanggalMulai = tglMulai,
                                    waktuMulai = wktMulai.ifBlank { "09.00" },
                                    waktuSelesai = wktSelesai.ifBlank { "-" },
                                    pewawancara = pewawancara.ifBlank { "-" },
                                    isToday = daysBetween == 0L
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        
                        InterviewAlarmScheduler.scheduleJadwalReminder(
                            context = context,
                            jadwalId = jadwalId,
                            judul = judul,
                            tanggalMulai = tglMulai,
                            waktuMulai = wktMulai.ifBlank { "09.00" },
                            pewawancara = pewawancara.ifBlank { "-" }
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
                    "Simpan Jadwal",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(Modifier.height(20.dp))


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
                val timeState = rememberTimePickerState(initialHour = 15, initialMinute =   0)
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
