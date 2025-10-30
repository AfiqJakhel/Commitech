package com.example.commitech.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.commitech.ui.viewmodel.InterviewStatus
import com.example.commitech.ui.viewmodel.PengumumanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UbahDetailScreen(
    navController: NavController,
    namaPeserta: String,
    viewModel: PengumumanViewModel
) {
    // 🧩 State untuk field
    var selectedDivisi by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Diterima") }

    // 🔹 Ambil daftar divisi dari ViewModel
    val daftarDivisi = viewModel.getAllDivisiNames()

    // 🔹 Ambil data peserta yang akan diedit
    LaunchedEffect(namaPeserta) {
        val peserta = viewModel.getAllParticipants().find { it.name == namaPeserta }
        if (peserta != null) {
            selectedDivisi = peserta.division
            selectedStatus =
                if (peserta.status == InterviewStatus.REJECTED) "Ditolak" else "Diterima"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 🔙 Tombol kembali
        Text(
            text = "←",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .clickable { navController.popBackStack() }
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Ubah Detil Peserta",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        // =====================================================
        // 🔽 DROPDOWN DIVISI (menggunakan ExposedDropdownMenuBox)
        // =====================================================
        Text("Divisi", style = MaterialTheme.typography.bodyMedium)
        var expandedDivisi by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expandedDivisi,
            onExpandedChange = { expandedDivisi = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedDivisi,
                onValueChange = {},
                readOnly = true,
                label = { Text("Pilih Divisi") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDivisi)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandedDivisi,
                onDismissRequest = { expandedDivisi = false }
            ) {
                daftarDivisi.forEach { divisi ->
                    DropdownMenuItem(
                        text = { Text(divisi) },
                        onClick = {
                            selectedDivisi = divisi
                            expandedDivisi = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // =====================================================
        // 🔽 DROPDOWN STATUS
        // =====================================================
        Text("Status", style = MaterialTheme.typography.bodyMedium)
        var expandedStatus by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expandedStatus,
            onExpandedChange = { expandedStatus = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedStatus,
                onValueChange = {},
                readOnly = true,
                label = { Text("Pilih Status") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandedStatus,
                onDismissRequest = { expandedStatus = false }
            ) {
                listOf("Diterima", "Ditolak").forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status) },
                        onClick = {
                            selectedStatus = status
                            expandedStatus = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(30.dp))

        // =====================================================
        // 💾 TOMBOL SIMPAN
        // =====================================================
        Button(
            onClick = {
                if (selectedDivisi.isNotBlank()) {
                    val newStatus =
                        if (selectedStatus == "Ditolak") InterviewStatus.REJECTED else InterviewStatus.ACCEPTED

                    viewModel.updateParticipantDivisionAndStatus(
                        name = namaPeserta,
                        divisi = selectedDivisi,
                        status = newStatus
                    )

                    // Navigasi balik
                    navController.popBackStack("pengumumanKelulusan", inclusive = true)
                    navController.navigate("pengumumanKelulusan")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Simpan Perubahan")
        }
    }
}
