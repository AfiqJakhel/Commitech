package com.example.commitech.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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

    val colorScheme = MaterialTheme.colorScheme
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Jadwal Rekrutmen",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
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
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ===== Kalender =====
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header bulan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { currentMonth = currentMonth.minusMonths(1) },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Bulan sebelumnya")
                        }
                        Text(
                            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("id"))} ${currentMonth.year}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { currentMonth = currentMonth.plusMonths(1) },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = colorScheme.primary
                            )
                        ) {
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
                                color = colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Grid tanggal
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        userScrollEnabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        items(daysInMonth) { date ->
                            val isToday = date == today
                            val isMarked = date in tanggalJadwal

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(3.dp)
                                    .background(
                                        when {
                                            isToday -> Brush.linearGradient(
                                                colors = listOf(
                                                    colorScheme.primary,
                                                    colorScheme.primary.copy(alpha = 0.8f)
                                                )
                                            )
                                            isMarked -> Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFFD1C4E9),
                                                    Color(0xFFE1BEE7)
                                                )
                                            )
                                            else -> Brush.linearGradient(
                                                colors = listOf(Color.Transparent, Color.Transparent)
                                            )
                                        },
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (isMarked && !isToday) 2.dp else 0.dp,
                                        color = Color(0xFF9C27B0),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    color = when {
                                        isToday -> Color.White
                                        isMarked -> Color(0xFF6A1B9A)
                                        else -> colorScheme.onSurface
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = if (isToday || isMarked) FontWeight.Bold else FontWeight.Normal
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
                    navController.navigate("tambahJadwal")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Tambah Jadwal",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }


            Spacer(Modifier.height(16.dp))

            // ===== Daftar Jadwal =====
            daftarJadwal.forEach { jadwal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .shadow(6.dp, RoundedCornerShape(18.dp)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFE1BEE7).copy(alpha = 0.3f),
                                        Color(0xFFCE93D8).copy(alpha = 0.2f)
                                    )
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = jadwal.judul,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = colorScheme.onSurface
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "${jadwal.tanggalMulai} - ${jadwal.tanggalSelesai}",
                                    fontSize = 14.sp,
                                    color = colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${jadwal.waktuMulai} - ${jadwal.waktuSelesai}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9C27B0),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            IconButton(
                                onClick = {
                                    navController.navigate("ubahJadwal/${jadwal.id}")
                                },
                                modifier = Modifier
                                    .background(
                                        Color(0xFF9C27B0).copy(alpha = 0.1f),
                                        CircleShape
                                    )
                                    .size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFF9C27B0),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (daftarJadwal.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surface.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada jadwal wawancara",
                            color = colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Spacer untuk padding bawah agar jadwal terakhir tidak terpotong
            Spacer(Modifier.height(16.dp))
        }
    }
}
