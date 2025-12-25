package com.example.commitech.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.commitech.ui.viewmodel.InterviewStatus
import com.example.commitech.ui.viewmodel.PengumumanViewModel
import com.example.commitech.ui.viewmodel.SeleksiWawancaraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UbahDetailScreen(
    navController: NavController,
    namaPeserta: String,
    viewModel: PengumumanViewModel,
    seleksiViewModel: SeleksiWawancaraViewModel,
    token: String? = null
) {
    var selectedDivisi by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Diterima") }
    val daftarDivisi = viewModel.getAllDivisiNames()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(namaPeserta) {
        val peserta = viewModel.getAllParticipants().find { it.name == namaPeserta }
        if (peserta != null) {
            selectedDivisi = peserta.division
            selectedStatus =
                if (peserta.status == InterviewStatus.REJECTED) "Ditolak" else "Diterima"
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Ubah Detail Peserta",
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
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(20.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Nama Peserta",
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    namaPeserta,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(32.dp))
            
            Text(
                "Informasi Detail",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colorScheme.onSurface
            )
            
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        "Divisi",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    
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
                            placeholder = { Text("Pilih Divisi", color = colorScheme.onSurface.copy(alpha = 0.5f)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDivisi)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedDivisi,
                            onDismissRequest = { expandedDivisi = false },
                            modifier = Modifier.background(colorScheme.surface)
                        ) {
                            daftarDivisi.forEach { divisi ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            divisi,
                                            fontWeight = FontWeight.Medium
                                        ) 
                                    },
                                    onClick = {
                                        selectedDivisi = divisi
                                        expandedDivisi = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        "Status",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    
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
                            placeholder = { Text("Pilih Status", color = colorScheme.onSurface.copy(alpha = 0.5f)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (selectedStatus == "Ditolak") 
                                    Color(0xFFD32F2F) else colorScheme.primary,
                                unfocusedBorderColor = if (selectedStatus == "Ditolak")
                                    Color(0xFFD32F2F).copy(alpha = 0.3f) else colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = if (selectedStatus == "Ditolak") Color(0xFFD32F2F) else colorScheme.onSurface
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false },
                            modifier = Modifier.background(colorScheme.surface)
                        ) {
                            listOf("Diterima", "Ditolak").forEach { status ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            status,
                                            fontWeight = FontWeight.Medium,
                                            color = if (status == "Ditolak") Color(0xFFD32F2F) else colorScheme.onSurface
                                        ) 
                                    },
                                    onClick = {
                                        selectedStatus = status
                                        expandedStatus = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            
            if (selectedStatus == "Ditolak") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Peserta akan dihapus dari daftar kelulusan",
                            fontSize = 13.sp,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            error,
                            fontSize = 13.sp,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Button(
                onClick = {
                    if (selectedDivisi.isNotBlank() && !isLoading) {
                        isLoading = true
                        errorMessage = null
                        
                        val newStatus =
                            if (selectedStatus == "Ditolak") InterviewStatus.REJECTED else InterviewStatus.ACCEPTED

                        viewModel.updateParticipantDivisionAndStatus(
                            name = namaPeserta,
                            divisi = selectedDivisi,
                            status = newStatus,
                            seleksiViewModel = seleksiViewModel,
                            token = token,
                            onSuccess = {
                                isLoading = false
                                viewModel.syncFromSeleksiWawancara(seleksiViewModel)
                                navController.popBackStack()
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary
                ),
                enabled = selectedDivisi.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Simpan Perubahan",
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
