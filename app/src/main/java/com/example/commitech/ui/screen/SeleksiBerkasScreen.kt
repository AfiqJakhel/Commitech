@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.commitech.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.commitech.ui.viewmodel.Peserta
import com.example.commitech.ui.viewmodel.SeleksiBerkasViewModel
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.animation.AnimatedContent



@Composable
fun SeleksiBerkasScreen(
    viewModel: SeleksiBerkasViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val pesertaList by viewModel.pesertaList.collectAsState()
    val isDark = isSystemInDarkTheme()

    // 🎨 Tema warna berdasarkan mode
    val backgroundColor = if (isDark) Color(0xFF121212) else Color(0xFFEAFBE9)
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

        // 🔹 Pakai Box agar tombol selalu “di atas” konten scroll
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding)
        ) {
            // 🔹 LazyColumn (scrollable content)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 120.dp) // ruang untuk tombol
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

            // 🔹 Tombol di bawah, selalu terlihat
            Button(
                onClick = { /* TODO: Export CSV */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding() // biar gak ketiban nav bar
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

    // Status: null = belum diproses, true = diterima, false = ditolak
    var status by remember { mutableStateOf<Boolean?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Nama peserta
            Text(
                text = "$index. ${peserta.nama}",
                fontSize = 16.sp,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            // === Jika belum diproses, tampilkan ikon Info / Check / Close ===
            if (status == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { showInfoDialog = true }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Lulus",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { showAcceptDialog = true }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tidak Lulus",
                        tint = Color(0xFFF44336),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { showRejectDialog = true }
                    )
                }
            } else {
                // === Kalau sudah diproses, ganti ikon dengan hasil ===
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val resultText = if (status == true) "Diterima" else "Ditolak"
                    val resultColor = if (status == true) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    val resultIcon =
                        if (status == true) Icons.Default.CheckCircle else Icons.Default.Close

                    Text(
                        text = resultText,
                        color = resultColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = resultIcon,
                        contentDescription = resultText,
                        tint = resultColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // 🔵 Pop-up Info
    if (showInfoDialog) {
        InfoDialog(
            peserta = peserta,
            onDismiss = { showInfoDialog = false }
        )
    }

    // 🟩 Pop-up Terima
    if (showAcceptDialog) {
        AcceptDialog(
            peserta = peserta,
            onDismiss = { showAcceptDialog = false },
            onConfirm = { reason ->
                status = true // ubah jadi "Diterima"
                showAcceptDialog = false
            }
        )
    }

    // 🟥 Pop-up Tolak
    if (showRejectDialog) {
        RejectDialog(
            peserta = peserta,
            onDismiss = { showRejectDialog = false },
            onConfirm = { reason ->
                status = false // ubah jadi "Ditolak"
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
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)) // efek gelap transparan
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
                Column(modifier = Modifier.padding(20.dp)) {

                    // Header nama + close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = peserta.nama,
                            color = Color(0xFF4A3A79),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFF4A3A79)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Detail pilihan divisi dan alasan
                    DetailRow("Pilihan Divisi 1", "Konsumsi")
                    DetailRow("Alasan Memilih Divisi 1", "Ingin menjadi bagian dari divisi konsumsi")
                    DetailRow("Pilihan Divisi 2", "Acara")
                    DetailRow("Alasan Memilih Divisi 2", "Ingin menjadi bagian dari divisi Acara")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checklist dokumen
                    ChecklistRow("Surat Komitmen", true)
                    ChecklistRow("CV", true)
                    ChecklistRow("KRS", true)
                }
            }
        }
    }
}

@Composable
fun AcceptDialog(
    peserta: Peserta,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
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

                    // ❌ Tombol close di kanan atas
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

                    // 🔹 Judul
                    Text(
                        text = "Anda Yakin Untuk\nMenerima Peserta ini?",
                        color = Color(0xFF4A3A79),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ✅ Ikon besar
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(70.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 💬 Kolom alasan
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

                    // 🟩 Tombol konfirmasi
                    Button(
                        onClick = {
                            if (reason.isNotBlank()) onConfirm(reason)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Terima",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
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

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
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

                    // 💬 Kolom alasan (tanpa OutlinedTextField)
                    TextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = {
                            Text(
                                "Tuliskan alasan anda...",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF2F2F2),
                            unfocusedContainerColor = Color(0xFFF2F2F2),
                            disabledContainerColor = Color(0xFFF2F2F2),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
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
                        Text(
                            text = "Tolak",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
