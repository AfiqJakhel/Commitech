package com.example.commitech.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.commitech.R
import com.example.commitech.ui.theme.LocalTheme

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
    onDataPendaftarClick: () -> Unit,
    onSeleksiBerkasClick: () -> Unit,
    onIsiJadwalClick: () -> Unit,
    onSeleksiWawancaraClick: () -> Unit,
    onKelulusanClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val themeCard = LocalTheme.current

    Scaffold(
        bottomBar = { HomeBottomBar() },
        containerColor = colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Header (Hello + Notifications + Settings)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello,",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorScheme.onBackground
                        )
                        Text(
                            text = "Pengguna",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onBackground
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 🔔 Notifikasi Button
                        IconButton(onClick = { navController.navigate("notifikasi") }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifikasi",
                                tint = colorScheme.onBackground,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // ⚙️ Settings Button
                        IconButton(onClick = { /* TODO: Arahkan ke halaman settings */ }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = colorScheme.onBackground,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 📊 Jumlah Pendaftar
                Text(
                    text = "Jumlah Pendaftar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Angka + Deskripsi sejajar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "35",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "jumlah total pendaftar untuk event, mencakup semua pendaftar dari berbagai tahap seleksi",
                        fontSize = 12.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1f)
                    )
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
    val gradientBrush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(backgroundColor, Color.White.copy(alpha = 0.5f)),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1050f, 0f)
    )

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .height(150.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .background(brush = gradientBrush)
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

@Composable
fun HomeBottomBar() {
    val colorScheme = MaterialTheme.colorScheme

    NavigationBar(containerColor = colorScheme.background) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = colorScheme.onBackground) },
            label = { Text("Home", color = colorScheme.onBackground) }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile", tint = colorScheme.onBackground) },
            label = { Text("Profile", color = colorScheme.onBackground) }
        )
    }
}
