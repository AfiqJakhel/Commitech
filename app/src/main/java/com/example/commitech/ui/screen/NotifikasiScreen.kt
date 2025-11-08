package com.example.commitech.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.navigation.NavController
import com.example.commitech.ui.viewmodel.JadwalViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val type: NotificationType,
    val icon: ImageVector,
    val isRead: Boolean = false
)

enum class NotificationType {
    URGENT,    // Merah - jadwal hari ini
    REMINDER,  // Kuning - jadwal besok
    INFO       // Biru - informasi umum
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifikasiScreen(navController: NavController, viewModel: JadwalViewModel) {
    val colorScheme = MaterialTheme.colorScheme
    val jadwalList = viewModel.daftarJadwal
    val dateFormatter = viewModel.formatter
    val hariIni = LocalDate.now()
    
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // Generate notifications from jadwal
    val notifications = remember(jadwalList) {
        jadwalList.mapNotNull { jadwal ->
            try {
                val mulai = LocalDate.parse(jadwal.tanggalMulai, dateFormatter)
                when (val daysBetween = ChronoUnit.DAYS.between(hariIni, mulai)) {
                    0L -> NotificationItem(
                        id = jadwal.id,
                        title = "Pengingat Urgent!",
                        message = "${jadwal.judul} berlangsung hari ini pada pukul ${jadwal.waktuMulai} - ${jadwal.waktuSelesai}",
                        time = "Hari ini",
                        type = NotificationType.URGENT,
                        icon = Icons.Default.Warning
                    )
                    1L -> NotificationItem(
                        id = jadwal.id,
                        title = "Pengingat Jadwal",
                        message = "${jadwal.judul} akan berlangsung besok pada pukul ${jadwal.waktuMulai} - ${jadwal.waktuSelesai}",
                        time = "Besok",
                        type = NotificationType.REMINDER,
                        icon = Icons.Default.Event
                    )
                    in 2..30 -> NotificationItem(
                        id = jadwal.id,
                        title = "Jadwal Mendatang",
                        message = "${jadwal.judul} akan berlangsung pada ${jadwal.tanggalMulai} pukul ${jadwal.waktuMulai}",
                        time = "$daysBetween hari lagi",
                        type = NotificationType.INFO,
                        icon = Icons.Default.Info
                    )
                    else -> null
                }
            } catch (e: Exception) {
                // Jika parsing gagal, tetap tampilkan notifikasi sebagai info
                NotificationItem(
                    id = jadwal.id,
                    title = "Jadwal: ${jadwal.judul}",
                    message = "Jadwal akan berlangsung pada ${jadwal.tanggalMulai} pukul ${jadwal.waktuMulai} - ${jadwal.waktuSelesai}",
                    time = "Mendatang",
                    type = NotificationType.INFO,
                    icon = Icons.Default.Event
                )
            }
        }.sortedBy { 
            when(it.type) {
                NotificationType.URGENT -> 0
                NotificationType.REMINDER -> 1
                NotificationType.INFO -> 2
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Notifikasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
        if (notifications.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    
                    Text(
                        text = "Tidak Ada Notifikasi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    
                    Text(
                        text = "Semua notifikasi akan muncul di sini",
                        fontSize = 14.sp,
                        color = colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500)) + slideInVertically { -30 }
                    ) {
                        NotificationSummaryCard(notificationCount = notifications.size)
                    }
                }
                
                items(notifications.size) { index ->
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(
                            tween(500, delayMillis = 100 + (index * 50))
                        ) + slideInVertically(
                            initialOffsetY = { 30 },
                            animationSpec = tween(500, delayMillis = 100 + (index * 50))
                        )
                    ) {
                        NotificationCard(notification = notifications[index])
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSummaryCard(notificationCount: Int) {
    val colorScheme = MaterialTheme.colorScheme
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column {
                    Text(
                        text = "Notifikasi Aktif",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "$notificationCount pengingat jadwal",
                        fontSize = 13.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Badge dengan bentuk circle yang lebih bagus
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$notificationCount",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    val colorScheme = MaterialTheme.colorScheme
    
    val (backgroundColor, iconColor) = when (notification.type) {
        NotificationType.URGENT -> Pair(
            Color(0xFFFFEBEE),
            Color(0xFFD32F2F)
        )
        NotificationType.REMINDER -> Pair(
            Color(0xFFFFF3E0),
            Color(0xFFF57C00)
        )
        NotificationType.INFO -> Pair(
            Color(0xFFE3F2FD),
            Color(0xFF1976D2)
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Section
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    notification.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            // Content Section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = iconColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = notification.time,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = iconColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
