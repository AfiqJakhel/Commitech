package com.example.commitech.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.navigation.NavController
import com.example.commitech.ui.viewmodel.InterviewStatus
import com.example.commitech.ui.viewmodel.PengumumanViewModel
import com.example.commitech.ui.viewmodel.ParticipantInfo
import com.example.commitech.ui.viewmodel.SeleksiWawancaraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengumumanScreen(
    navController: NavController,
    viewModel: PengumumanViewModel,
    seleksiViewModel: SeleksiWawancaraViewModel
) {
    var showPilihan by remember { mutableStateOf(false) }
    var selectedPeserta by remember { mutableStateOf<ParticipantInfo?>(null) }
    
    // State untuk track perubahan data
    var refreshTrigger by remember { mutableIntStateOf(0) }
    
    // Sync saat pertama kali masuk
    LaunchedEffect(Unit) {
        viewModel.syncFromSeleksiWawancara(seleksiViewModel)
        refreshTrigger++
    }
    
    // Polling untuk check perubahan data setiap 500ms
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            val currentAccepted = seleksiViewModel.getAllParticipants()
                .count { it.status == InterviewStatus.ACCEPTED && it.division.isNotBlank() }
            val currentInPengumuman = viewModel.daftarDivisi.sumOf { it.pesertaLulus.size }
            
            // Jika ada perbedaan, sync ulang
            if (currentAccepted != currentInPengumuman) {
                viewModel.syncFromSeleksiWawancara(seleksiViewModel)
                refreshTrigger++
            }
        }
    }
    
    val daftarDivisi = viewModel.daftarDivisi
    val totalPeserta = daftarDivisi.sumOf { it.pesertaLulus.size }
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pengumuman Kelulusan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
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
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            
            // Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
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
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Peserta Lulus",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "$totalPeserta Peserta",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorScheme.primary
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Tombol pilih
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Daftar Divisi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                TextButton(onClick = { showPilihan = !showPilihan }) {
                    Text(
                        if (showPilihan) "Tutup Pilihan" else "Pilih Peserta",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Daftar Divisi
            daftarDivisi.forEach { divisi ->
                var expanded by remember { mutableStateOf(false) }

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
                    Column(Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                Color(0xFF9C27B0).copy(alpha = 0.1f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            divisi.namaDivisi.take(1),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color(0xFF9C27B0)
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            divisi.namaDivisi,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = colorScheme.onSurface
                                        )
                                        Text(
                                            "${divisi.pesertaLulus.size} peserta • Koordinator: ${divisi.koordinator}",
                                            fontSize = 13.sp,
                                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                            Icon(
                                imageVector = if (expanded)
                                    Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = colorScheme.primary
                            )
                        }

                        if (expanded) {
                            Spacer(Modifier.height(16.dp))
                            Divider(color = colorScheme.onSurface.copy(alpha = 0.1f))
                            Spacer(Modifier.height(12.dp))
                            
                            if (divisi.pesertaLulus.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Belum ada peserta",
                                        color = colorScheme.onSurface.copy(alpha = 0.4f),
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                divisi.pesertaLulus.forEachIndexed { index, peserta ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(
                                                        colorScheme.primary.copy(alpha = 0.1f),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "${index + 1}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = colorScheme.primary
                                                )
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Text(
                                                peserta.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colorScheme.onSurface
                                            )
                                        }

                                        if (showPilihan) {
                                            RadioButton(
                                                selected = (selectedPeserta == peserta),
                                                onClick = { selectedPeserta = peserta },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = colorScheme.primary
                                                )
                                            )
                                        }
                                    }
                                    if (index < divisi.pesertaLulus.lastIndex) {
                                        Divider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            color = colorScheme.onSurface.copy(alpha = 0.05f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tombol Edit
            if (selectedPeserta != null) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        navController.navigate("ubahDetail/${selectedPeserta!!.name}")
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
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Edit Detail Peserta",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(Modifier.height(20.dp))
        }
    }
}
