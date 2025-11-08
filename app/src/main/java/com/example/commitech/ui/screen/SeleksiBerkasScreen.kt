@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.commitech.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.commitech.ui.viewmodel.Peserta
import com.example.commitech.ui.viewmodel.SeleksiBerkasViewModel

@Composable
fun SeleksiBerkasScreen(
    viewModel: SeleksiBerkasViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val pesertaList by viewModel.pesertaList.collectAsState()
    val isDark = isSystemInDarkTheme()

    // 🎨 Warna berdasarkan tema
    val backgroundColor = if (isDark) Color(0xFF121212) else Color.White
    val cardColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDark) Color(0xFFECECEC) else Color(0xFF1A1A1A)
    val subTitleColor = if (isDark) Color(0xFFBDBDBD) else Color(0xFF4A3A79)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Seleksi Berkas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = subTitleColor
                        )
                        Text(
                            text = "${pesertaList.size} Peserta",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = subTitleColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                itemsIndexed(pesertaList) { index, peserta ->
                    PesertaCard(
                        index = index + 1,
                        peserta = peserta,
                        cardColor = cardColor,
                        textColor = textColor
                    )
                }
            }

            Button(
                onClick = { /* TODO: Export CSV */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Export to .csv",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun PesertaCard(
    index: Int,
    peserta: Peserta,
    cardColor: Color,
    textColor: Color
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<Boolean?>(null) }

    val animatedBorderColor by animateColorAsState(
        targetValue = when (status) {
            true -> Color(0xFF4CAF50)
            false -> Color(0xFFF44336)
            else -> Color(0xFFE0E0E0)
        },
        label = "borderAnim"
    )
    val animatedShadow by animateDpAsState(
        targetValue = if (status != null) 6.dp else 2.dp,
        label = "shadowAnim"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(animatedShadow, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
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
                        peserta.nama,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                    // Info Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(
                                onClick = { showInfoDialog = true },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                // Status
                if (status != null) {
                    Text(
                        when (status) {
                            true -> "✓ Diterima"
                            false -> "✗ Ditolak"
                            else -> ""
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (status) {
                            true -> Color(0xFF4CAF50)
                            false -> Color(0xFFD32F2F)
                            else -> textColor
                        }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (status == null) {
                    // Tombol Terima
                    CircleIconButtonBerkas(
                        icon = Icons.Default.CheckCircle,
                        background = Color(0xFFE8F5E9),
                        tint = Color(0xFF1B5E20),
                        enabled = true
                    ) {
                        showAcceptDialog = true
                    }

                    // Tombol Tolak
                    CircleIconButtonBerkas(
                        icon = Icons.Default.Close,
                        background = Color(0xFFFFEBEE),
                        tint = Color(0xFFB71C1C),
                        enabled = true
                    ) {
                        showRejectDialog = true
                    }
                }
            }
        }
    }

    // 🔹 Dialog Pop-up
    if (showInfoDialog) {
        InfoDialog(
            peserta = peserta,
            onDismiss = { showInfoDialog = false }
        )
    }
    if (showAcceptDialog) {
        AcceptDialog(
            peserta = peserta,
            onDismiss = { showAcceptDialog = false },
            onConfirm = { _ ->
                status = true
                showAcceptDialog = false
            }
        )
    }
    if (showRejectDialog) {
        RejectDialog(
            peserta = peserta,
            onDismiss = { showRejectDialog = false },
            onConfirm = { _ ->
                status = false
                showRejectDialog = false
            }
        )
    }
}

@Composable
fun InfoDialog(
    peserta: Peserta,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 24.dp, vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // 🔹 Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = peserta.nama,
                            fontSize = 22.sp,
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // 🔸 Detail Informasi Horizontal
                    DetailRowInfo("Pilihan Divisi 1", "Konsumsi")
                    DetailRowInfo(
                        "Alasan Memilih Divisi 1",
                        "Ingin menjadi bagian dari divisi konsumsi"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DetailRowInfo("Pilihan Divisi 2", "Acara")
                    DetailRowInfo(
                        "Alasan Memilih Divisi 2",
                        "Ingin menjadi bagian dari divisi acara"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔹 Dokumen checklist
                    Text(
                        text = "Berkas yang Diupload:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A40),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    DetailRowCheck("Surat Komitmen")
                    DetailRowCheck("CV")
                    DetailRowCheck("KRS")
                }
            }
        }
    }
}

@Composable
fun DetailRowInfo(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A40),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1A1A40),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            lineHeight = 18.sp
        )
    }
}


@Composable
fun DetailRowCheck(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
            contentDescription = "Checked",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun AcceptDialog(
    peserta: Peserta,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false // ✅ bikin background hitam full
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)) // ✅ gelap penuh seperti InfoDialog
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF4A3A79)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Anda Yakin Untuk\nMenerima Peserta ini?",
                        color = Color(0xFF4A3A79),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(70.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = {
                            Text(
                                "Tuliskan alasan anda menerima peserta ini...",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF2F2F2),
                            unfocusedContainerColor = Color(0xFFF2F2F2),
                            focusedIndicatorColor = Color(0xFF4CAF50),
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { if (reason.isNotBlank()) onConfirm(reason) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Terima", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}



@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFF1A1A1A),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color(0xFF1A1A1A),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ChecklistRow(label: String, isChecked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF1A1A1A),
            fontSize = 14.sp
        )
        if (isChecked) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Checked",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


@Composable
fun RejectDialog(
    peserta: Peserta,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false // ✅ biar background hitam full juga
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)) // ✅ samain gelapnya
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF4A3A79)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Anda Yakin Untuk\nMenolak Peserta ini?",
                        color = Color(0xFF4A3A79),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(70.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    TextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = {
                            Text("Tuliskan alasan anda...", fontSize = 13.sp, color = Color.Gray)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF2F2F2),
                            unfocusedContainerColor = Color(0xFFF2F2F2),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { if (reason.isNotBlank()) onConfirm(reason) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tolak", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CircleIconButtonBerkas(
    icon: ImageVector,
    background: Color,
    tint: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (enabled) background else background.copy(alpha = 0.5f))
            .clickable(
                enabled = enabled,
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) tint else tint.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
