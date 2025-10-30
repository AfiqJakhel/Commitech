package com.example.commitech.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.commitech.ui.viewmodel.InterviewStatus
import com.example.commitech.ui.viewmodel.PengumumanViewModel
import com.example.commitech.ui.viewmodel.ParticipantInfo

@Composable
fun PengumumanScreen(
    navController: NavController,
    viewModel: PengumumanViewModel = viewModel()
) {
    val daftarDivisi = viewModel.daftarDivisi
    var showPilihan by remember { mutableStateOf(false) }
    var selectedPeserta by remember { mutableStateOf<ParticipantInfo?>(null) }

    val totalPeserta = daftarDivisi.sumOf { it.pesertaLulus.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 🔙 Back
        Text(
            text = "←",
            fontSize = 24.sp,
            modifier = Modifier.clickable { navController.popBackStack() }
        )

        Spacer(Modifier.height(8.dp))
        Text("Pengumuman Kelulusan", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "$totalPeserta Peserta",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        // Tombol pilih utama
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { showPilihan = !showPilihan }) {
                Text(if (showPilihan) "Tutup Pilihan" else "Pilih", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        // List Divisi
        daftarDivisi.forEach { divisi ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    // Header Divisi
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(divisi.namaDivisi, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
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

        // Tombol Edit (muncul hanya jika peserta dipilih)
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
