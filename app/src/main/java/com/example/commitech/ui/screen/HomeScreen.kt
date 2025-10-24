package com.example.commitech.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.commitech.R
import com.example.commitech.ui.theme.TealLight
import androidx.compose.foundation.lazy.items


data class HomeCardData(
    val title: String,
    val description: String,
    val backgroundColor: Color,
    val imageRes: Int,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    onDataPendaftarClick: () -> Unit,
    onSeleksiBerkasClick: () -> Unit,
    onIsiJadwalClick: () -> Unit,
    onSeleksiWawancaraClick: () -> Unit,
    onKelulusanClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

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

                // 🔹 Header (Hello + Settings sejajar)
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
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(26.dp)
                    )
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
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealLight
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
                    Color(0xFFFFE18E),
                    R.drawable.datapendaftar,
                    onDataPendaftarClick
                ),
                HomeCardData(
                    "Seleksi Berkas",
                    "lihat total pendaftar yang sudah masuk ke seleksi berkas.",
                    Color(0xFFB8E9F9),
                    R.drawable.landingpage,
                    onSeleksiBerkasClick
                ),
                HomeCardData(
                    "Isi Jadwal Wawancara",
                    "Tempat panitia mengintegrasikan jadwal yang lulus wawancara.",
                    Color(0xFFB6F9A8),
                    R.drawable.jadwalwawancara,
                    onIsiJadwalClick
                ),
                HomeCardData(
                    "Seleksi Wawancara",
                    "lihat peserta yang lulus wawancara.",
                    Color(0xFFFFC6C6),
                    R.drawable.wawancara,
                    onSeleksiWawancaraClick
                ),
                HomeCardData(
                    "Pengumuman Kelulusan",
                    "Selamat untuk para peserta yang lulus acara ini.",
                    Color(0xFFF9B4F3),
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
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(20.dp), // 🔹 lebih besar sedikit dari sebelumnya
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
                color = Color(0xFF2B2B2B),
                fontSize = 18.sp // 🔹 lebih besar dari 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = Color(0xFF3A3A3A),
                fontSize = 14.sp // 🔹 sedikit lebih besar agar proporsional
            )
        }

        // 🔹 Gambar lebih besar (dari 80.dp → 100.dp)
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}


@Composable
fun HomeBottomBar() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") }
        )
    }
}