package com.example.commitech.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.commitech.ui.theme.LocalTheme
import com.example.commitech.ui.viewmodel.DataPendaftarViewModel
import com.example.commitech.ui.viewmodel.Pendaftar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataPendaftarScreen(
    viewModel: DataPendaftarViewModel,
    onBackClick: () -> Unit
) {
    val customColors = LocalTheme.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Data Pendaftar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF1A1A40)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Import Excel
            Button(
                onClick = { /* Aksi Import Excel */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Import Excel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card Jumlah Pendaftar dengan Gradient
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    customColors.DataPendaftar,
                                    Color.White.copy(alpha = 0.5f)
                                ),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1050f, 0f)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${viewModel.dummyPendaftarList.size}",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A40)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pendaftar",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF1A1A40)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Header Kolom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No.",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Nama",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "NIM",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(40.dp)) // Space untuk ikon info
            }

            Spacer(modifier = Modifier.height(8.dp))

            // LazyColumn (Daftar Pendaftar)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(viewModel.dummyPendaftarList) { index, pendaftar ->
                    PendaftarItem(
                        index = index + 1,
                        pendaftar = pendaftar
                    )
                }
            }
        }
    }
}

@Composable
fun PendaftarItem(
    index: Int,
    pendaftar: Pendaftar
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nomor Urut
            Text(
                text = "$index.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A40),
                modifier = Modifier.width(32.dp)
            )

            // Nama
            Text(
                text = pendaftar.nama,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF1A1A40),
                modifier = Modifier.weight(1f)
            )

            // NIM
            Text(
                text = pendaftar.nim,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF1A1A40),
                modifier = Modifier.width(100.dp)
            )

            // Tombol Info
            IconButton(
                onClick = { showDialog = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Detail",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // Dialog Detail Pendaftar
    if (showDialog) {
        DetailPendaftarDialog(
            pendaftar = pendaftar,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun DetailPendaftarDialog(
    pendaftar: Pendaftar,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header dengan nama dan tombol close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = pendaftar.nama,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A40),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF666666)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detail Informasi
                DetailRowPendaftar(
                    label = "Pilihan Divisi 1",
                    value = "Konsumsi"
                )

                DetailRowPendaftar(
                    label = "Alasan Memilih Divisi 1",
                    value = "ingin menjadi bagian\ndari divisi konsumsi"
                )

                Spacer(modifier = Modifier.height(8.dp))

                DetailRowPendaftar(
                    label = "Pilihan Divisi 2",
                    value = "Acara"
                )

                DetailRowPendaftar(
                    label = "Alasan Memilih Divisi 1",
                    value = "ingin menjadi bagian\ndari divisi Acara"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Status dengan checkmark
                DetailRowWithCheck(label = "Surat Komitmen")
                DetailRowWithCheck(label = "CV")
                DetailRowWithCheck(label = "KRS")
            }
        }
    }
}

@Composable
fun DetailRowPendaftar(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1A1A40),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1A1A40),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DetailRowWithCheck(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1A1A40)
        )
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Completed",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(24.dp)
        )
    }
}