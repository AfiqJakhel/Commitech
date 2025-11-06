package com.example.commitech.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.commitech.ui.viewmodel.JadwalViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalRekrutmenScreen(
    navController: NavController,
    viewModel: JadwalViewModel = viewModel()
) {
    val daftarJadwal = viewModel.daftarJadwal

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    val daysInMonth = remember(currentMonth) {
        (1..currentMonth.lengthOfMonth()).map { currentMonth.atDay(it) }
    }

    // Ambil semua tanggal yang ada jadwal
    val tanggalJadwal = daftarJadwal.flatMap { jadwal ->
        try {
            val start = LocalDate.parse(jadwal.tanggalMulai, viewModel.formatter)
            val end = LocalDate.parse(jadwal.tanggalSelesai, viewModel.formatter)
            generateSequence(start) { d -> if (d < end) d.plusDays(1) else null }
                .plus(end)
                .toList()
        } catch (e: Exception) {
            emptyList() // kalau parsing gagal, lewati saja jadwal ini
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Jadwal Rekrutmen",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A40)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color(0xFF1A1A40))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ===== Kalender =====
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    // Header bulan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Bulan sebelumnya")
                        }
                        Text(
                            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("id"))} ${currentMonth.year}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A40)
                        )
                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Bulan berikutnya")
                        }
                    }

                    // Hari
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach {
                            Text(
                                text = it,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Grid tanggal
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        userScrollEnabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(daysInMonth) { date ->
                            val isToday = date == today
                            val isMarked = date in tanggalJadwal

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .background(
                                        when {
                                            isToday -> Color(0xFF1976D2)
                                            isMarked -> Color(0xFFD1C4E9)
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (isMarked) 1.dp else 0.dp,
                                        color = Color(0xFF7B1FA2),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    color = if (isToday) Color.White else Color(0xFF1A1A40),
                                    fontSize = 13.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ===== Tombol Tambah Jadwal =====
            Button(
                onClick = {
                    // ✅ Navigasi ke halaman tambah jadwal di dalam jadwal_graph
                    navController.navigate("tambahJadwal")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text("+ Tambah Jadwal", color = Color.White)
            }


            Spacer(Modifier.height(16.dp))

            // ===== Daftar Jadwal =====
            daftarJadwal.forEach { jadwal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "${jadwal.tanggalMulai} - ${jadwal.tanggalSelesai}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A40)
                        )
                        Text(
                            text = jadwal.judul,
                            color = Color(0xFF1A1A40),
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                navController.navigate("ubahJadwal/${jadwal.id}")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFFE91E63)
                                )
                            }
                        }
                    }
                }
            }

            if (daftarJadwal.isEmpty()) {
                Text(
                    "Belum ada jadwal.",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}
