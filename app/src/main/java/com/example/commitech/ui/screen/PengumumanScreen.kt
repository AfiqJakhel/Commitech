package com.example.commitech.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.commitech.ui.viewmodel.InterviewStatus
import com.example.commitech.ui.viewmodel.PengumumanViewModel
import com.example.commitech.ui.viewmodel.ParticipantInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengumumanScreen(
    navController: NavController,
    viewModel: PengumumanViewModel = viewModel()
) {
    val daftarDivisi = viewModel.daftarDivisi
    var showPilihan by remember { mutableStateOf(false) }
    var selectedPeserta by remember { mutableStateOf<ParticipantInfo?>(null) }

    val totalPeserta = daftarDivisi.sumOf { it.pesertaLulus.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengumuman Kelulusan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(48.dp) // 🔹 tombol back besar
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(32.dp) // 🔹 ikon back besar
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // 🔹 Header Info Peserta
            Text(
                text = "Pengumuman Kelulusan",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$totalPeserta Peserta",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            // 🔹 Tombol pilih
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showPilihan = !showPilihan }) {
                    Text(if (showPilihan) "Tutup Pilihan" else "Pilih", fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // 🔹 Daftar Divisi
            daftarDivisi.forEach { divisi ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    divisi.namaDivisi,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                                if (expanded) {
                                    Text(
                                        "Koordinator: ${divisi.koordinator}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (expanded)
                                    Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }

                        if (expanded) {
                            Spacer(Modifier.height(8.dp))
                            divisi.pesertaLulus.forEach { peserta ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(peserta.name)

                                    if (showPilihan) {
                                        RadioButton(
                                            selected = (selectedPeserta == peserta),
                                            onClick = { selectedPeserta = peserta }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 🔹 Tombol Edit
            if (selectedPeserta != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        navController.navigate("ubahDetail/${selectedPeserta!!.name}")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Edit")
                }
            }
        }
    }
}
