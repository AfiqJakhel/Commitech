package com.example.commitech.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.commitech.ui.components.CircleIconButton
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
    val pendaftarState by viewModel.pendaftarList.collectAsState()

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
                            text = "${pendaftarState.size}",
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

            // LazyColumn (Daftar Pendaftar)
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(pendaftarState) { index, pendaftar ->
                    PendaftarItem(
                        index = index + 1,
                        pendaftar = pendaftar,
                        onDelete = { pendaftarToDelete ->
                            viewModel.deletePendaftar(pendaftarToDelete)
                        },
                        onUpdate = { pendaftarToUpdate ->
                            viewModel.updatePendaftar(pendaftarToUpdate)
                        },
                        onEdit = { pendaftarBaru ->
                            viewModel.editPendaftar(pendaftarBaru)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PendaftarItem(
    index: Int,
    pendaftar: Pendaftar,
    onDelete: (Pendaftar) -> Unit,
    onUpdate: (Pendaftar) -> Unit,
    onEdit: (Pendaftar) -> Unit
) {
    var showDetailDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val animatedBorderColor by animateColorAsState(
        targetValue = Color(0xFFE0E0E0),
        label = "borderAnim"
    )
    val animatedShadow by animateDpAsState(
        targetValue = 2.dp,
        label = "shadowAnim"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(animatedShadow, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        border = BorderStroke(2.dp, animatedBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Nama + Info Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        pendaftar.nama,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    
                    // Info Button - sama besar dengan icon lain
                    CircleIconButton(
                        icon = Icons.Default.Info,
                        background = Color(0xFFE3F2FD),
                        tint = Color(0xFF1976D2),
                        enabled = true
                    ) {
                        showDetailDialog = true
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // NIM
                    Text(
                        pendaftar.nim,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Tombol Edit
                CircleIconButton(
                    icon = Icons.Default.Edit,
                    background = Color(0xFFF3E5F5),
                    tint = Color(0xFF4A148C),
                    enabled = true
                ) {
                    showEditDialog = true
                }

                // Tombol Delete
                CircleIconButton(
                    icon = Icons.Default.Delete,
                    background = Color(0xFFFFEBEE),
                    tint = Color(0xFFB71C1C),
                    enabled = true
                ) {
                    showDeleteDialog = true
                }
            }
        }
    }

    // Dialog Detail Pendaftar
    if (showDetailDialog) {
        DetailPendaftarDialog(
            pendaftar = pendaftar,
            onDismiss = { showDetailDialog = false },
            onDelete = {
                onDelete(pendaftar)
                showDetailDialog = false
            },
            onUpdate = {
                showDetailDialog = false
                showEditDialog = true
            }
        )
    }

    // Dialog Edit Pendaftar
    if (showEditDialog) {
        EditPendaftarDialog(
            pendaftar = pendaftar,
            onDismiss = { showEditDialog = false },
            onSave = onEdit
        )
    }
    
    // Dialog Konfirmasi Hapus
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            pendaftarName = pendaftar.nama,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDelete(pendaftar)
                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun DetailPendaftarDialog(
    pendaftar: Pendaftar,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: () -> Unit
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

                // Detail Informasi (Menggunakan data dari Pendaftar)
                DetailRowPendaftar(
                    label = "Pilihan Divisi 1",
                    value = pendaftar.divisi1
                )

                DetailRowPendaftar(
                    label = "Alasan Memilih Divisi 1",
                    value = pendaftar.alasan1
                )

                Spacer(modifier = Modifier.height(8.dp))

                DetailRowPendaftar(
                    label = "Pilihan Divisi 2",
                    value = pendaftar.divisi2
                )

                DetailRowPendaftar(
                    label = "Alasan Memilih Divisi 2",
                    value = pendaftar.alasan2
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Status dengan checkmark
                DetailRowWithCheck(label = "Surat Komitmen")
                DetailRowWithCheck(label = "CV")
                DetailRowWithCheck(label = "KRS")

                // Tombol Update & Delete
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tombol Update (PERBAIKAN: Spacer dikurangi menjadi 4.dp)
                    Button(
                        onClick = onUpdate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF57C00) // Warna oranye
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Update",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp)) // <-- PERBAIKAN DI SINI
                            Text(
                                "Update",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Tombol Delete (PERBAIKAN: Spacer dikurangi menjadi 4.dp)
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.error // Warna merah
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp)) // <-- PERBAIKAN DI SINI
                            Text(
                                "Hapus",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditPendaftarDialog(
    pendaftar: Pendaftar,
    onDismiss: () -> Unit,
    onSave: (Pendaftar) -> Unit
) {
    var nama by remember { mutableStateOf(pendaftar.nama) }
    var nim by remember { mutableStateOf(pendaftar.nim) }
    var divisi1 by remember { mutableStateOf(pendaftar.divisi1) }
    var alasan1 by remember { mutableStateOf(pendaftar.alasan1) }
    var divisi2 by remember { mutableStateOf(pendaftar.divisi2) }
    var alasan2 by remember { mutableStateOf(pendaftar.alasan2) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header dengan gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    colorScheme.primary,
                                    colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Edit Pendaftar",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nama Field
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { nama = it },
                        label = { 
                            Text(
                                "Nama",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.3f),
                            focusedLabelColor = colorScheme.primary
                        )
                    )

                    // NIM Field
                    OutlinedTextField(
                        value = nim,
                        onValueChange = { nim = it },
                        label = { 
                            Text(
                                "NIM",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.3f),
                            focusedLabelColor = colorScheme.primary
                        )
                    )

                    // Divider
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilihan Divisi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )

                    // Divisi 1 Field
                    OutlinedTextField(
                        value = divisi1,
                        onValueChange = { divisi1 = it },
                        label = { 
                            Text(
                                "Pilihan Divisi 1",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.3f),
                            focusedLabelColor = colorScheme.primary
                        )
                    )

                    // Alasan Divisi 1
                    OutlinedTextField(
                        value = alasan1,
                        onValueChange = { alasan1 = it },
                        label = { 
                            Text(
                                "Alasan Divisi 1",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.3f),
                            focusedLabelColor = colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Divisi 2 Field
                    OutlinedTextField(
                        value = divisi2,
                        onValueChange = { divisi2 = it },
                        label = { 
                            Text(
                                "Pilihan Divisi 2",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.3f),
                            focusedLabelColor = colorScheme.primary
                        )
                    )

                    // Alasan Divisi 2
                    OutlinedTextField(
                        value = alasan2,
                        onValueChange = { alasan2 = it },
                        label = { 
                            Text(
                                "Alasan Divisi 2",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.3f),
                            focusedLabelColor = colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tombol Batal
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.surface,
                                contentColor = colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "Batal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Tombol Simpan
                        Button(
                            onClick = {
                                val pendaftarBaru = pendaftar.copy(
                                    id = pendaftar.id,
                                    nama = nama,
                                    nim = nim,
                                    divisi1 = divisi1,
                                    alasan1 = alasan1,
                                    divisi2 = divisi2,
                                    alasan2 = alasan2
                                )
                                onSave(pendaftarBaru)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Simpan",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
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

@Composable
fun DeleteConfirmationDialog(
    pendaftarName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Warning
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Color(0xFFFFEBEE),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Title
                Text(
                    text = "Hapus Data?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Message
                Text(
                    text = "Apakah Anda yakin ingin menghapus data pendaftar",
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Nama Pendaftar
                Text(
                    text = "\"$pendaftarName\"?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tombol Batal
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.surface,
                            contentColor = colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.3f))
                    ) {
                        Text(
                            "Batal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Tombol Hapus
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Hapus",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}