@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.commitech.ui.screen

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.commitech.ui.theme.LocalTheme
import com.example.commitech.ui.viewmodel.Peserta
import com.example.commitech.ui.viewmodel.SeleksiBerkasViewModel
import com.example.commitech.ui.viewmodel.SeleksiBerkasViewModelFactory
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun SeleksiBerkasScreen(
    authViewModel: com.example.commitech.ui.viewmodel.AuthViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val viewModel: SeleksiBerkasViewModel = viewModel(
        factory = SeleksiBerkasViewModelFactory(context)
    )

    val authState by authViewModel.authState.collectAsState()
    
    // Set token dan load data saat pertama kali
    LaunchedEffect(Unit) {
        authState.token?.let { token ->
            viewModel.setAuthToken(token)
        }
    }
    val pesertaList by viewModel.pesertaList.collectAsState()
    val state by viewModel.state.collectAsState()
    val isLoading = state.isLoading
    val error = state.error
    val isDark = isSystemInDarkTheme()
    val themeCard = LocalTheme.current
    
    // Tampilkan error jika ada
    LaunchedEffect(error) {
        error?.let {
            // Error sudah di-handle di ViewModel, bisa ditampilkan di UI jika perlu
        }
    }

    // 🎨 Warna berdasarkan tema
    val backgroundColor = if (isDark) Color(0xFF121212) else Color.White
    val cardColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDark) Color(0xFFECECEC) else Color(0xFF1A1A1A)
    val subTitleColor = if (isDark) Color(0xFFBDBDBD) else Color(0xFF4A3A79)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var exportSuccess by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var exportedFileUri by remember { mutableStateOf<String?>(null) }



    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            if (isLoading) {
                // Loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF4A3A79)
                )
            } else if (error != null) {
                // Error message
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: $error",
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { viewModel.refresh() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3A79))
                    ) {
                        Text("Coba Lagi", color = Color.White)
                    }
                }
            } else if (pesertaList.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Belum ada data pendaftar",
                        color = textColor,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Silakan import data dari Excel atau tunggu pendaftar mendaftar",
                        color = subTitleColor,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(bottom = 180.dp)
                ) {
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor) // ✅ background penutup full
                                .padding(top = 8.dp, bottom = 8.dp)
                        ) {
                            JumlahPesertaCard(total = state.totalItems)
                        }
                    }
                    itemsIndexed(pesertaList) { index, peserta ->
                        val actualIndex = ((state.currentPage - 1) * 20) + index + 1
                        PesertaCard(
                            index = actualIndex,
                            peserta = peserta,
                            cardColor = cardColor,
                            textColor = textColor,
                            viewModel = viewModel,
                            authToken = authState.token
                        )
                    }

                    // Pagination Info & Controls
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Pagination Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Halaman ${state.currentPage} dari ${state.totalPages}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                val startItem = if (pesertaList.isNotEmpty()) {
                                    ((state.currentPage - 1) * 20) + 1
                                } else {
                                    0
                                }
                                val endItem = if (pesertaList.isNotEmpty()) {
                                    ((state.currentPage - 1) * 20) + pesertaList.size
                                } else {
                                    0
                                }

                                if (startItem > 0 && endItem > 0) {
                                    Text(
                                        text = "Menampilkan $startItem-$endItem dari ${state.totalItems}",
                                        fontSize = 12.sp,
                                        color = subTitleColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Pagination Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Previous Button
                                    Button(
                                        onClick = {
                                            if (state.currentPage > 1) {
                                                viewModel.loadPesertaFromDatabase(state.currentPage - 1, append = false)
                                            }
                                        },
                                        enabled = state.currentPage > 1 && !state.isLoading,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4A3A79),
                                            disabledContainerColor = Color(0xFFBDBDBD)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            "Sebelumnya",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Next Button
                                    Button(
                                        onClick = {
                                            viewModel.loadNextPage()
                                        },
                                        enabled = state.hasMore && !state.isLoading,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4A3A79),
                                            disabledContainerColor = Color(0xFFBDBDBD)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            "Selanjutnya",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Export Button - Hanya enabled jika semua peserta sudah direview
            val semuaSudahDireview = viewModel.semuaPesertaSudahDireview

            // ================= EXPORT BUTTON AREA =================
            when {

                // ===== LOADING STATE =====
                isExporting -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .navigationBarsPadding()
                    ) {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFBDBDBD)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Mengekspor data...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF22C55E)
                        )
                    }
                }

                // ===== BELUM SEMUA DIREVIEW =====
                !semuaSudahDireview -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBDBDBD)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .navigationBarsPadding()
                    ) {
                        Text(
                            text = "Selesaikan Review Semua Peserta untuk Export",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ===== SEMUA DIREVIEW → CSV & PDF =====
                else -> {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // ===== CSV BUTTON =====
                        Button(
                            onClick = {
                                isExporting = true
                                viewModel.exportToCSV { success, message, fileUri ->
                                    isExporting = false
                                    exportSuccess = success
                                    exportMessage = message
                                    exportedFileUri = fileUri
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22C55E)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Export CSV",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        // ===== PDF BUTTON =====
                        Button(
                            onClick = {
                                isExporting = true
                                viewModel.exportToPDF { success, message, fileUri ->
                                    isExporting = false
                                    exportSuccess = success
                                    exportMessage = message
                                    exportedFileUri = fileUri
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Export PDF",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            exportMessage?.let { message ->
                ExportResultDialog(
                    success = exportSuccess,
                    message = message,
                    onDismiss = {
                        exportMessage = null
                        exportedFileUri = null
                    },
                    onOpenFile = if (exportSuccess && exportedFileUri != null) {
                        {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(Uri.parse(exportedFileUri), "*/*")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
fun JumlahPesertaCard(
    total: Int,
) {
    val themeCard = LocalTheme.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7FA))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            themeCard.SeleksiBerkas,
                            Color.White.copy(alpha = 0.5f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1050f, 0f)
                    )
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$total",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A40)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Peserta",
                    fontSize = 28.sp,
                    color = Color(0xFF1A1A40)
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
    textColor: Color,
    viewModel: SeleksiBerkasViewModel,
    authToken: String?
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Collect peserta list untuk mendapatkan update terbaru
    val pesertaList by viewModel.pesertaList.collectAsState()
    val pesertaTerbaru = remember(pesertaList) {
        pesertaList.find { it.nama == peserta.nama } ?: peserta
    }

    // Gunakan status dari peserta terbaru
    val status = if (pesertaTerbaru.lulusBerkas) true else if (pesertaTerbaru.ditolak) false else null

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
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(2.dp, animatedBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Nama + NIM (kiri atas) dan Menu 3 titik (kanan atas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Nama dan NIM di kiri atas
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = pesertaTerbaru.nama,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    // NIM di sebelah nama
                    if (!pesertaTerbaru.nim.isNullOrBlank()) {
                        Text(
                            text = "• ${pesertaTerbaru.nim}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                }

                // Menu 3 titik di kanan atas
                var showMenu by remember { mutableStateOf(false) }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = textColor.copy(alpha = 0.7f)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Lihat Detail") },
                            onClick = {
                                showMenu = false
                                showInfoDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                showEditDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus", color = Color(0xFFD32F2F)) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer: Button Terima dan Tolak di tengah (hanya jika belum direview)
            if (status == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Terima
                    Button(
                        onClick = { showAcceptDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Terima",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Terima", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    // Tombol Tolak
                    Button(
                        onClick = { showRejectDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .padding(start = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tolak",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Tolak", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                // Jika sudah direview, tampilkan status badge di tengah
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (status) {
                        true -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        false -> Color(0xFFD32F2F).copy(alpha = 0.15f)
                        else -> Color.Transparent
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when (status) {
                            true -> "✓ Diterima"
                            false -> "✗ Ditolak"
                            else -> ""
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            true -> Color(0xFF2E7D32)
                            false -> Color(0xFFB71C1C)
                            else -> textColor
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // 🔹 Dialog Pop-up
    if (showInfoDialog) {
        InfoDialog(
            peserta = pesertaTerbaru,
            onDismiss = { showInfoDialog = false }
        )
    }
    if (showEditDialog) {
        EditPesertaDialog(
            peserta = pesertaTerbaru,
            onDismiss = { showEditDialog = false },
            onSave = { pesertaBaru ->
                viewModel.editPendaftar(authToken, pesertaBaru)
                showEditDialog = false
            }
        )
    }
    if (showDeleteDialog) {
        DeletePesertaDialog(
            pesertaName = pesertaTerbaru.nama,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deletePendaftar(authToken, pesertaTerbaru)
                showDeleteDialog = false
            }
        )
    }
    if (showAcceptDialog) {
        AcceptDialog(
            peserta = pesertaTerbaru,
            onDismiss = { showAcceptDialog = false },
            onConfirm = { _ ->
                viewModel.updatePesertaStatus(pesertaTerbaru.nama, lulusBerkas = true, ditolak = false)
                showAcceptDialog = false
            }
        )
    }
    if (showRejectDialog) {
        RejectDialog(
            peserta = pesertaTerbaru,
            onDismiss = { showRejectDialog = false },
            onConfirm = { _ ->
                viewModel.updatePesertaStatus(pesertaTerbaru.nama, lulusBerkas = false, ditolak = true)
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val isDark = isSystemInDarkTheme()

    val backgroundColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDark) Color(0xFFECECEC) else Color(0xFF1A1A1A)
    val subTitleColor = if (isDark) Color(0xFFBDBDBD) else Color(0xFF666666)
    val cardColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(horizontal = 16.dp, vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header dengan gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF4A3A79),
                                        Color(0xFF4A3A79).copy(alpha = 0.8f)
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = peserta.nama,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (!peserta.nim.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "NIM: ${peserta.nim}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Scrollable Content
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Informasi Pribadi
                        SectionCard(
                            title = "Informasi Pribadi",
                            cardColor = cardColor,
                            textColor = textColor,
                            subTitleColor = subTitleColor
                        ) {
                            DetailRowInfo("Email", peserta.email ?: "-", textColor, subTitleColor)
                            DetailRowInfo("Telepon", peserta.telepon ?: "-", textColor, subTitleColor)
                            DetailRowInfo("Jurusan", peserta.jurusan ?: "-", textColor, subTitleColor)
                            DetailRowInfo("Angkatan", peserta.angkatan ?: "-", textColor, subTitleColor)
                        }

                        // Pilihan Divisi 1
                        SectionCard(
                            title = "Pilihan Divisi 1",
                            cardColor = cardColor,
                            textColor = textColor,
                            subTitleColor = subTitleColor
                        ) {
                            DetailRowInfo("Divisi", peserta.divisi1 ?: "-", textColor, subTitleColor)
                            DetailRowInfo(
                                "Alasan",
                                peserta.alasan1 ?: "-",
                                textColor,
                                subTitleColor
                            )
                        }

                        // Pilihan Divisi 2
                        SectionCard(
                            title = "Pilihan Divisi 2",
                            cardColor = cardColor,
                            textColor = textColor,
                            subTitleColor = subTitleColor
                        ) {
                            DetailRowInfo("Divisi", peserta.divisi2 ?: "-", textColor, subTitleColor)
                            DetailRowInfo(
                                "Alasan",
                                peserta.alasan2 ?: "-",
                                textColor,
                                subTitleColor
                            )
                        }

                        // Dokumen & Lampiran
                        SectionCard(
                            title = "Dokumen & Lampiran",
                            cardColor = cardColor,
                            textColor = textColor,
                            subTitleColor = subTitleColor
                        ) {
                            // KRS Terakhir
                            if (!peserta.krsTerakhir.isNullOrBlank()) {
                                AttachmentRow(
                                    label = "KRS Terakhir",
                                    link = peserta.krsTerakhir,
                                    textColor = textColor,
                                    onLinkClick = {
                                        try {
                                            uriHandler.openUri(peserta.krsTerakhir)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Gagal membuka link: ${e.message}",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            } else {
                                DetailRowInfo("KRS Terakhir", "Tidak tersedia", textColor, subTitleColor.copy(alpha = 0.6f))
                            }

                            // Formulir Pendaftaran
                            if (!peserta.formulirPendaftaran.isNullOrBlank() && peserta.formulirPendaftaran != "false" && peserta.formulirPendaftaran != "null") {
                                if (peserta.formulirPendaftaran.startsWith("http")) {
                                    AttachmentRow(
                                        label = "Formulir Pendaftaran",
                                        link = peserta.formulirPendaftaran,
                                        textColor = textColor,
                                        onLinkClick = {
                                            try {
                                                uriHandler.openUri(peserta.formulirPendaftaran)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Gagal membuka link: ${e.message}",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    )
                                } else {
                                    DetailRowInfo("Formulir Pendaftaran", peserta.formulirPendaftaran, textColor, subTitleColor)
                                }
                            } else {
                                DetailRowInfo("Formulir Pendaftaran", "Tidak tersedia", textColor, subTitleColor.copy(alpha = 0.6f))
                            }

                            // Surat Komitmen
                            if (!peserta.suratKomitmen.isNullOrBlank() && peserta.suratKomitmen != "false" && peserta.suratKomitmen != "null") {
                                if (peserta.suratKomitmen.startsWith("http")) {
                                    AttachmentRow(
                                        label = "Surat Komitmen",
                                        link = peserta.suratKomitmen,
                                        textColor = textColor,
                                        onLinkClick = {
                                            try {
                                                uriHandler.openUri(peserta.suratKomitmen)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Gagal membuka link: ${e.message}",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    )
                                } else {
                                    DetailRowInfo("Surat Komitmen", peserta.suratKomitmen, textColor, subTitleColor)
                                }
                            } else {
                                DetailRowInfo("Surat Komitmen", "Tidak tersedia", textColor, subTitleColor.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    cardColor: Color,
    textColor: Color,
    subTitleColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Divider(
                color = subTitleColor.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            content()
        }
    }
}

@Composable
fun AttachmentRow(
    label: String,
    link: String,
    textColor: Color,
    onLinkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onLinkClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Buka Link",
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Buka",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DetailRowInfo(label: String, value: String, textColor: Color = Color(0xFF1A1A40), subTitleColor: Color = Color(0xFF666666)) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = subTitleColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = textColor,
            lineHeight = 20.sp
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
                                "Tuliskan alasan anda menerima peserta ini...(opsional)",
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
                        onClick = { onConfirm(reason) },
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
fun EditPesertaDialog(
    peserta: Peserta,
    onDismiss: () -> Unit,
    onSave: (Peserta) -> Unit
) {
    var nama by remember { mutableStateOf(peserta.nama) }
    var nim by remember { mutableStateOf(peserta.nim ?: "") }
    var divisi1 by remember { mutableStateOf(peserta.divisi1 ?: "") }
    var alasan1 by remember { mutableStateOf(peserta.alasan1 ?: "") }
    var divisi2 by remember { mutableStateOf(peserta.divisi2 ?: "") }
    var alasan2 by remember { mutableStateOf(peserta.alasan2 ?: "") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header dengan gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4A3A79),
                                    Color(0xFF4A3A79).copy(alpha = 0.8f)
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
                            Text("Nama", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A3A79),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF4A3A79)
                        )
                    )

                    // NIM Field
                    OutlinedTextField(
                        value = nim,
                        onValueChange = { nim = it },
                        label = {
                            Text("NIM", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A3A79),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF4A3A79)
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilihan Divisi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A3A79)
                    )

                    // Divisi 1 Field
                    OutlinedTextField(
                        value = divisi1,
                        onValueChange = { divisi1 = it },
                        label = {
                            Text("Pilihan Divisi 1", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A3A79),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF4A3A79)
                        )
                    )

                    // Alasan Divisi 1
                    OutlinedTextField(
                        value = alasan1,
                        onValueChange = { alasan1 = it },
                        label = {
                            Text("Alasan Divisi 1", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A3A79),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF4A3A79)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Divisi 2 Field
                    OutlinedTextField(
                        value = divisi2,
                        onValueChange = { divisi2 = it },
                        label = {
                            Text("Pilihan Divisi 2", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A3A79),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF4A3A79)
                        )
                    )

                    // Alasan Divisi 2
                    OutlinedTextField(
                        value = alasan2,
                        onValueChange = { alasan2 = it },
                        label = {
                            Text("Alasan Divisi 2", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A3A79),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedLabelColor = Color(0xFF4A3A79)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                val pesertaBaru = peserta.copy(
                                    nama = nama,
                                    nim = nim,
                                    divisi1 = divisi1,
                                    alasan1 = alasan1,
                                    divisi2 = divisi2,
                                    alasan2 = alasan2
                                )
                                onSave(pesertaBaru)
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
                                Text("Simpan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeletePesertaDialog(
    pesertaName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                Text(
                    text = "Hapus Data?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Apakah Anda yakin ingin menghapus data pendaftar",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "\"$pesertaName\"?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

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
                        Text("Hapus", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ExportResultDialog(
    success: Boolean,
    message: String,
    onDismiss: () -> Unit,
    onOpenFile: (() -> Unit)? = null
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

                    // ===== CLOSE ICON =====
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

                    // ===== TITLE =====
                    Text(
                        text = if (success) "Export Berhasil" else "Export Gagal",
                        color = Color(0xFF4A3A79),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ===== ICON =====
                    Icon(
                        imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (success) Color(0xFF16A34A) else Color(0xFFDC2626),
                        modifier = Modifier.size(70.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ===== MESSAGE =====
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ===== ACTION BUTTONS =====
                    if (success && onOpenFile != null) {
                        Button(
                            onClick = onOpenFile,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16A34A)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Buka File",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tutup", fontWeight = FontWeight.Bold)
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
