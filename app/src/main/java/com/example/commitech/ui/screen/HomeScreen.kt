package com.example.commitech.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.commitech.R
import com.example.commitech.ui.theme.LocalTheme
import com.example.commitech.ui.viewmodel.JadwalViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class HomeCardData(
    val title: String,
    val description: String,
    val backgroundColor: Color,
    val imageRes: Int,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    navController: androidx.navigation.NavController,
    authViewModel: com.example.commitech.ui.viewmodel.AuthViewModel,
    jadwalViewModel: JadwalViewModel,
    dataPendaftarViewModel: com.example.commitech.ui.viewmodel.DataPendaftarViewModel,
    onDataPendaftarClick: () -> Unit,
    onSeleksiBerkasClick: () -> Unit,
    onIsiJadwalClick: () -> Unit,
    onSeleksiWawancaraClick: () -> Unit,
    onKelulusanClick: () -> Unit,
    onAboutUsClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val themeCard = LocalTheme.current
    val authState by authViewModel.authState.collectAsState()
    val dataPendaftarState by dataPendaftarViewModel.state.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    
    // Load data pendaftar saat HomeScreen dibuka
    // Set authToken ke JadwalViewModel saat token tersedia
    LaunchedEffect(authState.token) {
        authState.token?.let { token ->
            jadwalViewModel.setAuthToken(token)
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
        // Load jumlah pendaftar dari backend
        dataPendaftarViewModel.loadPendaftarList(authState.token)
    }
    
    // Hitung jumlah notifikasi dari jadwal
    val jadwalList = jadwalViewModel.daftarJadwal
    val dateFormatter = jadwalViewModel.formatter
    val today = LocalDate.now()
    
    val notificationCount = jadwalList.count { jadwal ->
        try {
            val jadwalDate = LocalDate.parse(jadwal.tanggalMulai, dateFormatter)
            val daysBetween = ChronoUnit.DAYS.between(today, jadwalDate)
            daysBetween in 0..30 // Jadwal dalam 30 hari ke depan
        } catch (e: Exception) {
            false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 16.dp)
    ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Enhanced Header with Animation
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { -50 },
                        animationSpec = tween(500)
                    ) + fadeIn(animationSpec = tween(500))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    colorScheme.primary,
                                                    colorScheme.secondary
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "P",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                        text = "Hello,",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = colorScheme.onBackground.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = authState.user?.name ?: "Admin BEM",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colorScheme.onBackground
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 🔔 Notifikasi Button with animation and badge
                                var notifScale by remember { mutableFloatStateOf(1f) }
                                val notifScaleAnim by animateFloatAsState(
                                    targetValue = notifScale,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "notifScale"
                                )
                                
                                IconButton(
                                    onClick = { 
                                        navController.navigate("notifikasi")
                                        notifScale = 0.8f
                                    },
                                    modifier = Modifier.scale(notifScaleAnim)
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (notificationCount > 0) {
                                                Badge(
                                                    containerColor = Color(0xFFD32F2F),
                                                    contentColor = Color.White
                                                ) {
                                                    Text(
                                                        text = if (notificationCount > 9) "9+" else notificationCount.toString(),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Notifikasi",
                                            tint = colorScheme.onBackground
                                        )
                                    }
                                }
                                
                                // ⚙️ Settings Button with animation
                                var settingsScale by remember { mutableFloatStateOf(1f) }
                                val settingsScaleAnim by animateFloatAsState(
                                    targetValue = settingsScale,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "settingsScale"
                                )
                                
                                IconButton(
                                    onClick = { 
                                        onSettingsClick()
                                        settingsScale = 0.8f
                                    },
                                    modifier = Modifier.scale(settingsScaleAnim)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                
                                LaunchedEffect(notifScale) {
                                    if (notifScale != 1f) {
                                        delay(100)
                                        notifScale = 1f
                                    }
                                }
                                
                                LaunchedEffect(settingsScale) {
                                    if (settingsScale != 1f) {
                                        delay(100)
                                        settingsScale = 1f
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 📊 Jumlah Pendaftar with Animation
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInHorizontally(
                        initialOffsetX = { -100 },
                        animationSpec = tween(600, delayMillis = 200)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Jumlah Pendaftar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Angka + Deskripsi sejajar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    colorScheme.primary,
                                                    colorScheme.secondary
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dataPendaftarState.totalItems.toString(),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = "jumlah total pendaftar untuk event, mencakup semua pendaftar dari berbagai tahap seleksi",
                                    fontSize = 13.sp,
                                    color = colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 🔸 Kartu Menu (klik masing-masing)
            val cards = listOf(
                HomeCardData(
                    "Data Pendaftar",
                    "lihat data pendaftar yang sudah masuk.",
                    themeCard.DataPendaftar,
                    R.drawable.datapendaftar,
                    onDataPendaftarClick
                ),
                HomeCardData(
                    "Seleksi Berkas",
                    "lihat total pendaftar yang sudah masuk ke seleksi berkas.",
                    themeCard.SeleksiBerkas,
                    R.drawable.landingpage,
                    onSeleksiBerkasClick
                ),
                HomeCardData(
                    "Isi Jadwal Wawancara",
                    "Tempat panitia mengintegrasikan jadwal yang lulus wawancara.",
                    themeCard.JadwalWawancara,
                    R.drawable.jadwalwawancara,
                    onIsiJadwalClick
                ),
                HomeCardData(
                    "Seleksi Wawancara",
                    "lihat peserta yang lulus wawancara.",
                    themeCard.SeleksiWawancara,
                    R.drawable.wawancara,
                    onSeleksiWawancaraClick
                ),
                HomeCardData(
                    "Pengumuman Kelulusan",
                    "Selamat untuk para peserta yang lulus acara ini.",
                    themeCard.PengumumanKelulusan,
                    R.drawable.pengumumanlulus,
                    onKelulusanClick
                )
            )
            items(cards) { card ->
                HomeCard(
                    title = card.title,
                    description = card.description,
                    backgroundColor = card.backgroundColor,
                    imageRes = card.imageRes,
                    onClick = card.onClick
                )
            }
        }
}

@Composable
fun HomeCard(
    title: String,
    description: String,
    backgroundColor: Color,
    imageRes: Int,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .height(150.dp)
            .scale(scale),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                delay(100)
                isPressed = false
            }
        }
        Row(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(backgroundColor, Color.White.copy(alpha = 0.5f)),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1050f, 0f)
                    )
                )
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    color = colorScheme.onSurface.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
        }
    }
}

