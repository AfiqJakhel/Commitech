package com.example.commitech.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.navigation.NavController
import com.example.commitech.ui.viewmodel.InterviewStatus
import com.example.commitech.ui.viewmodel.PengumumanViewModel
import com.example.commitech.ui.viewmodel.ParticipantInfo
import com.example.commitech.ui.viewmodel.SeleksiWawancaraViewModel
import com.example.commitech.ui.viewmodel.AuthViewModel
import com.example.commitech.ui.viewmodel.AuthState
import com.example.commitech.data.model.HasilWawancaraResponse
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.commitech.utils.PdfGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengumumanScreen(
    navController: NavController,
    viewModel: PengumumanViewModel,
    seleksiViewModel: SeleksiWawancaraViewModel,
    authViewModel: AuthViewModel? = null
) {
    var showPilihan by remember { mutableStateOf(false) }
    var selectedPeserta by remember { mutableStateOf<ParticipantInfo?>(null) }

    // State untuk track perubahan data
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Get auth token - menggunakan pattern yang sama seperti DetailJadwalWawancaraScreen
    val authState by authViewModel?.authState?.collectAsState() ?: remember {
        mutableStateOf(AuthState())
    }
    val authToken = authState.token

    // Load data dari database melalui SeleksiWawancaraViewModel
    val hasilWawancaraList by seleksiViewModel.hasilWawancaraList.collectAsState()

    // Load data hasil wawancara saat screen dibuka
    LaunchedEffect(Unit) {
        authToken?.let { token ->
            seleksiViewModel.loadHasilWawancaraAndUpdateStatus(token, forceReload = true)
        }
    }

    // Update UI saat hasilWawancaraList berubah
    LaunchedEffect(hasilWawancaraList) {
        // Load dari database hasil wawancara yang sudah diterima
        // Hapus semua peserta yang ada terlebih dahulu
        viewModel.daftarDivisi.forEach { it.pesertaLulus.clear() }

        // Tambahkan peserta yang diterima dari database
        // Filter: status == "diterima" (divisi bisa null, akan ditampilkan di divisi yang sesuai atau "Lainnya")
        hasilWawancaraList.filter {
            it.status == "diterima"
        }.forEach { hasil ->
            // Jika divisi tidak null/blank, cari divisi yang sesuai
            // Jika divisi null/blank, tetap tampilkan (bisa ditambahkan ke divisi pertama atau "Lainnya")
            val divisiName = if (!hasil.divisi.isNullOrBlank()) {
                hasil.divisi
            } else {
                // Jika tidak ada divisi, tambahkan ke divisi pertama yang tersedia
                viewModel.daftarDivisi.firstOrNull()?.namaDivisi ?: "Lainnya"
            }

            val targetDivisi = viewModel.daftarDivisi.find { it.namaDivisi == divisiName }
            if (targetDivisi != null) {
                targetDivisi.pesertaLulus.add(
                    ParticipantInfo(
                        name = hasil.namaPeserta,
                        status = InterviewStatus.ACCEPTED,
                        division = divisiName
                    )
                )
            } else {
                // Jika divisi tidak ditemukan, tambahkan ke divisi pertama
                viewModel.daftarDivisi.firstOrNull()?.pesertaLulus?.add(
                    ParticipantInfo(
                        name = hasil.namaPeserta,
                        status = InterviewStatus.ACCEPTED,
                        division = divisiName
                    )
                )
            }
        }
        refreshTrigger++
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

            // Header Card dengan gradient yang lebih menarik
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    colorScheme.primary,
                                    colorScheme.primary.copy(alpha = 0.9f),
                                    colorScheme.primary.copy(alpha = 0.8f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Total Peserta Lulus",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "$totalPeserta Peserta",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            if (totalPeserta > 0) {
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Selamat kepada peserta yang lulus!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Section header dengan style yang lebih menarik
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        "Daftar Divisi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                }
                Surface(
                    onClick = { showPilihan = !showPilihan },
                    shape = RoundedCornerShape(12.dp),
                    color = if (showPilihan) colorScheme.primaryContainer else Color.Transparent,
                    modifier = Modifier.clickable { showPilihan = !showPilihan }
                ) {
                    Text(
                        if (showPilihan) "Tutup" else "Pilih",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (showPilihan) colorScheme.onPrimaryContainer else colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Daftar Divisi dengan animasi
            daftarDivisi.forEach { divisi ->
                var expanded by remember { mutableStateOf(false) }
                val rotationAngle by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = tween(150),
                    label = "rotation"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(
                                animationSpec = tween(200)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Avatar dengan gradient sesuai tema
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    colorScheme.primary,
                                                    colorScheme.primary.copy(alpha = 0.8f)
                                                )
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        divisi.namaDivisi.take(1),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text(
                                        divisi.namaDivisi,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (divisi.pesertaLulus.size > 0)
                                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                                            else Color(0xFFBDBDBD).copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                "${divisi.pesertaLulus.size} peserta",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (divisi.pesertaLulus.size > 0)
                                                    Color(0xFF4CAF50)
                                                else Color(0xFF757575),
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 4.dp
                                                )
                                            )
                                        }
                                        Text(
                                            "•",
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                        Text(
                                            divisi.koordinator,
                                            fontSize = 13.sp,
                                            color = colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer { rotationZ = rotationAngle }
                            )
                        }

                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandVertically(
                                animationSpec = tween(200)
                            ) + fadeIn(
                                animationSpec = tween(200)
                            ),
                            exit = shrinkVertically(
                                animationSpec = tween(200)
                            ) + fadeOut(
                                animationSpec = tween(200)
                            )
                        ) {
                            Column {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    color = colorScheme.onSurface.copy(alpha = 0.1f),
                                    thickness = 1.dp
                                )

                                if (divisi.pesertaLulus.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.People,
                                                contentDescription = null,
                                                tint = colorScheme.onSurface.copy(alpha = 0.3f),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Text(
                                                "Belum ada peserta",
                                                color = colorScheme.onSurface.copy(alpha = 0.5f),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        divisi.pesertaLulus.forEachIndexed { index, peserta ->
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (index % 2 == 0)
                                                    colorScheme.surfaceContainerHighest
                                                else Color.Transparent,
                                                onClick = {
                                                    if (showPilihan) {
                                                        selectedPeserta = peserta
                                                    }
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            12.dp
                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = colorScheme.primaryContainer,
                                                            modifier = Modifier.size(36.dp)
                                                        ) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Text(
                                                                    "${index + 1}",
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 14.sp,
                                                                    color = colorScheme.onPrimaryContainer
                                                                )
                                                            }
                                                        }
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
                                            }
                                        }
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

            // Tombol Export PDF dengan design yang lebih menarik
            Spacer(Modifier.height(28.dp))
            val context = LocalContext.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (totalPeserta > 0) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFD32F2F),
                                        Color(0xFFC62828)
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFBDBDBD),
                                        Color(0xFF9E9E9E)
                                    )
                                )
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Button(
                        onClick = {
                            if (totalPeserta > 0) {
                                PdfGenerator.generatePengumumanPDF(
                                    context = context,
                                    daftarDivisi = daftarDivisi,
                                    onSuccess = { uri ->
                                        // Share PDF
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            putExtra(
                                                Intent.EXTRA_SUBJECT,
                                                "Pengumuman Kelulusan Seleksi"
                                            )
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Berikut adalah pengumuman kelulusan seleksi BEM KM FTI"
                                            )
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(
                                                shareIntent,
                                                "Bagikan PDF"
                                            )
                                        )
                                        Toast.makeText(
                                            context,
                                            "PDF berhasil dibuat!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Tidak ada peserta yang lulus untuk diekspor",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = totalPeserta > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    "Export PDF Pengumuman",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (totalPeserta > 0) {
                                    Text(
                                        "Download dan bagikan pengumuman",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}
