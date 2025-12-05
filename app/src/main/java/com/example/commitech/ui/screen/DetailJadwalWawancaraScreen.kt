package com.example.commitech.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.commitech.ui.viewmodel.Jadwal
import com.example.commitech.ui.viewmodel.JadwalViewModel
import com.example.commitech.ui.viewmodel.Peserta
import com.example.commitech.ui.viewmodel.SeleksiBerkasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailJadwalWawancaraScreen(
    navController: NavController,
    jadwal: Jadwal,
    seleksiBerkasViewModel: SeleksiBerkasViewModel,
    jadwalViewModel: JadwalViewModel,
    authViewModel: com.example.commitech.ui.viewmodel.AuthViewModel? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    // Collect pesertaDiterima sebagai State untuk reactive updates
    val pesertaDiterima by seleksiBerkasViewModel.pesertaList.collectAsState()
    val pesertaDiterimaFiltered = remember(pesertaDiterima) {
        pesertaDiterima.filter { it.statusSeleksiBerkas == "lulus" }
    }
    
    // Ambil peserta yang sudah dipilih untuk jadwal ini
    val pesertaTerpilih = jadwalViewModel.getPesertaByJadwalId(jadwal.id)
    
    // State untuk dialog pemilihan peserta
    var showDialogPilihPeserta by remember { mutableStateOf(false) }
    
    // Load peserta dari database saat screen dibuka
    LaunchedEffect(jadwal.id) {
        jadwalViewModel.loadPesertaFromJadwal(jadwal.id)
        
        // Set token ke SeleksiBerkasViewModel untuk load peserta dari database
        // Pastikan hanya peserta dengan status "lulus" yang ditampilkan
        authViewModel?.authState?.value?.token?.let { token ->
            seleksiBerkasViewModel.setAuthToken(token)
        }
    }
    
    // Refresh peserta diterima saat data berubah
    LaunchedEffect(seleksiBerkasViewModel.pesertaList) {
        // Data sudah otomatis ter-update karena menggunakan StateFlow
        // pesertaDiterima akan otomatis memfilter peserta dengan lulusBerkas = true
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = jadwal.judul,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorScheme.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Pewawancara: ${jadwal.pewawancara}",
                                fontSize = 14.sp,
                                color = colorScheme.onBackground.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = jadwal.tanggalMulai,
                                fontSize = 13.sp,
                                color = colorScheme.onBackground.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = " • ",
                                fontSize = 13.sp,
                                color = colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "${jadwal.waktuMulai} - ${jadwal.waktuSelesai}",
                                fontSize = 13.sp,
                                color = colorScheme.onBackground.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Peserta Wawancara",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Button(
                        onClick = { showDialogPilihPeserta = true },
                        enabled = pesertaDiterimaFiltered.isNotEmpty() && pesertaTerpilih.size < 5,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Tambah Peserta")
                    }
                }
            }
            
            item {
                // Debug info
                Text(
                    text = "Debug: pesertaDiterima=${seleksiBerkasViewModel.pesertaDiterima.size} peserta, pesertaTerpilih=${pesertaTerpilih.size}",
                    color = Color.Red,
                    fontSize = 12.sp
                )
                Text(
                    text = "${pesertaTerpilih.size}/5 peserta dipilih",
                    fontSize = 14.sp,
                    color = colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (pesertaTerpilih.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Belum ada peserta yang dipilih",
                                    color = colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Klik 'Tambah Peserta' untuk memilih peserta",
                                    color = colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            } else {
                items(pesertaTerpilih) { peserta ->
                    PesertaCardWawancara(
                        peserta = peserta,
                        colorScheme = colorScheme,
                        onHapus = {
                            // Hapus peserta dari jadwal (akan menghapus dari database juga)
                            jadwalViewModel.hapusPesertaDariJadwal(jadwal.id, peserta.nama)
                        }
                    )
                }
            }
        }
    }
    
    // Dialog untuk memilih peserta
    if (showDialogPilihPeserta) {
        DialogPilihPeserta(
            pesertaDiterima = pesertaDiterimaFiltered,
            pesertaTerpilih = pesertaTerpilih,
            onDismiss = { showDialogPilihPeserta = false },
            onConfirm = { selectedPeserta ->
                jadwalViewModel.setPesertaUntukJadwal(jadwal.id, selectedPeserta)
                showDialogPilihPeserta = false
            },
            colorScheme = colorScheme
        )
    }
}

@Composable
fun PesertaCardWawancara(
    peserta: Peserta,
    colorScheme: ColorScheme,
    onHapus: (() -> Unit)? = null
) {
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = peserta.nama.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = peserta.nama,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Lulus Seleksi Berkas",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Button hapus selalu ditampilkan jika onHapus tidak null
            IconButton(
                onClick = { onHapus?.invoke() },
                modifier = Modifier.size(40.dp),
                enabled = onHapus != null
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hapus Peserta",
                    tint = if (onHapus != null) Color(0xFFD32F2F) else colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DialogPilihPeserta(
    pesertaDiterima: List<Peserta>,
    pesertaTerpilih: List<Peserta>,
    onDismiss: () -> Unit,
    onConfirm: (List<Peserta>) -> Unit,
    colorScheme: ColorScheme
) {
    val selectedPeserta = remember { mutableStateListOf<Peserta>().apply { addAll(pesertaTerpilih) } }
    val maxPeserta = 5
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Peserta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "Pilih maksimal $maxPeserta peserta (${selectedPeserta.size}/$maxPeserta)",
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // List peserta
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pesertaDiterima) { peserta ->
                        val isSelected = selectedPeserta.any { it.nama == peserta.nama }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        selectedPeserta.removeAll { it.nama == peserta.nama }
                                    } else {
                                        if (selectedPeserta.size < maxPeserta) {
                                            // Cek apakah peserta sudah ada berdasarkan nama
                                            if (!selectedPeserta.any { it.nama == peserta.nama }) {
                                                selectedPeserta.add(peserta)
                                            }
                                        }
                                    }
                                }
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) 
                                    colorScheme.primary.copy(alpha = 0.1f) 
                                else 
                                    colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                colorScheme.primary.copy(alpha = 0.1f),
                                                RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = peserta.nama.take(1).uppercase(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = peserta.nama,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        // Tampilkan status dari database
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = "Lulus Seleksi Berkas",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF4CAF50),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Dipilih",
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Button konfirmasi
                Button(
                    onClick = { onConfirm(selectedPeserta) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedPeserta.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Simpan (${selectedPeserta.size} peserta)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

