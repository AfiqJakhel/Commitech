package com.example.commitech.ui.screen

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.commitech.ui.viewmodel.*
import com.example.commitech.ui.viewmodel.DetailJadwalWawancaraViewModel
import com.example.commitech.ui.viewmodel.PesertaWawancaraState
import com.example.commitech.data.model.HasilWawancaraResponse
import com.example.commitech.notification.InterviewNotificationHelper

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

@Composable
fun DialogPilihWaktu(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    colorScheme: ColorScheme
) {
    var selectedMinutes by remember { mutableStateOf(6) }
    val timeOptions = listOf(5, 6, 10, 15, 20, 30, 45, 60)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
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
                Text(
                    text = "Pilih Durasi Wawancara",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(timeOptions) { minutes ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMinutes = minutes },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedMinutes == minutes)
                                    colorScheme.primary.copy(alpha = 0.1f)
                                else
                                    colorScheme.surface
                            )
                        ) {
                            Text(
                                text = "$minutes menit",
                                fontSize = 16.sp,
                                fontWeight = if (selectedMinutes == minutes) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedMinutes == minutes) colorScheme.primary else colorScheme.onSurface,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = { onConfirm(selectedMinutes) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Mulai")
                    }
                }
            }
        }
    }
}

@Composable
fun DialogPilihDivisi(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    colorScheme: ColorScheme
) {
    var selectedDivisi by remember { mutableStateOf("") }
    val divisions = listOf("Acara", "Humas", "Konsumsi", "Perlengkapan")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
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
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Pilih Divisi Penerimaan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )

                Text(
                    "Peserta akan ditempatkan di divisi ini",
                    fontSize = 13.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(divisions) { divisi ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDivisi = divisi },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedDivisi == divisi)
                                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                                else
                                    colorScheme.surface
                            )
                        ) {
                            Text(
                                text = divisi,
                                fontSize = 16.sp,
                                fontWeight = if (selectedDivisi == divisi) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedDivisi == divisi) Color(0xFF4CAF50) else colorScheme.onSurface,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (selectedDivisi.isNotEmpty()) {
                                onConfirm(selectedDivisi)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = selectedDivisi.isNotEmpty()
                    ) {
                        Text("Terima")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailJadwalWawancaraScreen(
    navController: NavController,
    jadwal: Jadwal,
    seleksiBerkasViewModel: SeleksiBerkasViewModel,
    jadwalViewModel: JadwalViewModel,
    authViewModel: com.example.commitech.ui.viewmodel.AuthViewModel? = null,
    seleksiWawancaraViewModel: SeleksiWawancaraViewModel? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val detailViewModel: DetailJadwalWawancaraViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val authState by authViewModel?.authState?.collectAsState() ?: remember { mutableStateOf(com.example.commitech.ui.viewmodel.AuthState()) }
    val authToken = authState.token

    val pesertaDiterima by seleksiBerkasViewModel.pesertaList.collectAsState()

    val hasilWawancaraList by seleksiWawancaraViewModel?.hasilWawancaraList?.collectAsState() ?: remember { mutableStateOf(emptyList<HasilWawancaraResponse>()) }

    val pesertaIdDiHasilWawancara = remember(hasilWawancaraList) {
        hasilWawancaraList.map { it.pesertaId }.toSet()
    }

    val pesertaPerJadwalUpdateTrigger by jadwalViewModel.pesertaPerJadwalUpdateTrigger.collectAsState()

    val pesertaTerpilih = remember(jadwal.id, pesertaPerJadwalUpdateTrigger) {
        jadwalViewModel.getPesertaByJadwalId(jadwal.id)
    }

    val isSavingHasil by detailViewModel.isSavingHasil.collectAsState()
    val saveHasilError by detailViewModel.saveHasilError.collectAsState()
    val saveHasilSuccess by detailViewModel.saveHasilSuccess.collectAsState()

    var pesertaToDelete by remember { mutableStateOf<Peserta?>(null) }

    LaunchedEffect(pesertaTerpilih.size, pesertaPerJadwalUpdateTrigger) {
        pesertaTerpilih.forEach { peserta ->
            detailViewModel.initPesertaState(peserta)
        }
    }

    LaunchedEffect(pesertaTerpilih) {
        pesertaTerpilih.forEach { peserta ->
            detailViewModel.initPesertaState(peserta)
        }
    }

    LaunchedEffect(saveHasilError) {
        saveHasilError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Long)
            }
        }
    }

    var pesertaToRemove by remember { mutableStateOf<Peserta?>(null) }

    LaunchedEffect(saveHasilSuccess) {
        saveHasilSuccess?.let { success ->
            scope.launch {
                snackbarHostState.showSnackbar(success, duration = SnackbarDuration.Long)

                authToken?.let { token ->
                    delay(300)
                    seleksiWawancaraViewModel?.loadHasilWawancaraAndUpdateStatus(token, forceReload = true)
                    jadwalViewModel.loadPesertaFromJadwal(jadwal.id)
                }

                pesertaToRemove = null
            }
        }
    }

    val pesertaDiJadwalLain = remember(jadwal.id, jadwalViewModel.pesertaPerJadwal) {
        jadwalViewModel.getAllPesertaNamaDiJadwalLain(jadwal.id)
    }

    val pesertaDiterimaFiltered = remember(pesertaDiterima, pesertaDiJadwalLain, pesertaIdDiHasilWawancara, hasilWawancaraList) {
        pesertaDiterima.filter { peserta ->
            val lulusBerkas = peserta.statusSeleksiBerkas == "lulus"

            val belumAdaDiJadwal = peserta.nama !in pesertaDiJadwalLain

            val belumPunyaJadwal = peserta.tanggalJadwal.isNullOrBlank()

            val belumFinal = if (peserta.id == null) {
                true
            } else {
                val hasilWawancara = hasilWawancaraList.find { it.pesertaId == peserta.id }
                hasilWawancara == null || (hasilWawancara.status != "diterima" && hasilWawancara.status != "ditolak")
            }

            lulusBerkas && belumAdaDiJadwal && belumPunyaJadwal && belumFinal
        }
    }

    var showDialogPilihPeserta by remember { mutableStateOf(false) }

    val timerTick by detailViewModel.timerTick.collectAsState()

    val pesertaPending = remember(pesertaTerpilih, timerTick, pesertaPerJadwalUpdateTrigger, hasilWawancaraList) {
        pesertaTerpilih
    }

    LaunchedEffect(jadwal.id, jadwalViewModel.daftarJadwal.size) {
        jadwalViewModel.loadPesertaFromJadwal(jadwal.id)

        jadwalViewModel.daftarJadwal.forEachIndexed { index, otherJadwal ->
            if (otherJadwal.id != jadwal.id) {
                delay(index * 100L)
                jadwalViewModel.loadPesertaFromJadwal(otherJadwal.id)
            }
        }

        authToken?.let { token ->
            seleksiBerkasViewModel.setAuthToken(token)
            seleksiWawancaraViewModel?.loadHasilWawancaraAndUpdateStatus(token)
        }
    }

    LaunchedEffect(seleksiBerkasViewModel.pesertaList) {
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
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Lokasi: ${jadwal.lokasi.ifBlank { "-" }}",
                            fontSize = 13.sp,
                            color = colorScheme.onBackground.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Normal
                        )
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
                        enabled = pesertaTerpilih.size < 5 && pesertaDiterimaFiltered.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            disabledContainerColor = colorScheme.primary.copy(alpha = 0.6f)
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
                Text(
                    text = "${pesertaTerpilih.size}/5 peserta dipilih",
                    fontSize = 14.sp,
                    color = colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (pesertaPending.isEmpty()) {
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
                items(
                    items = pesertaPending,
                    key = { peserta -> peserta.id ?: peserta.nama }
                ) { peserta ->
                    val pesertaState = detailViewModel.getPesertaState(peserta)
                    PesertaCardWawancara(
                        peserta = peserta,
                        pesertaState = pesertaState,
                        colorScheme = colorScheme,
                        detailViewModel = detailViewModel,
                        authToken = authToken,
                        jadwalViewModel = jadwalViewModel,
                        jadwalId = jadwal.id,
                        seleksiWawancaraViewModel = seleksiWawancaraViewModel,
                        onTerimaTolak = {
                            pesertaToRemove = peserta
                        },
                        onHapus = {
                            pesertaToDelete = peserta
                        },
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }

    pesertaToDelete?.let { peserta ->
        AlertDialog(
            onDismissRequest = { pesertaToDelete = null },
            title = {
                Text(
                    text = "Hapus Peserta",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Yakin ingin menghapus ${peserta.nama} dari jadwal ini?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authToken?.let { token ->
                            jadwalViewModel.setAuthToken(token)
                        }
                        jadwalViewModel.hapusPesertaDariJadwal(jadwal.id, peserta)
                        pesertaToDelete = null
                    }
                ) {
                    Text(
                        text = "Hapus",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pesertaToDelete = null }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDialogPilihPeserta) {
        LaunchedEffect(showDialogPilihPeserta) {
            authToken?.let { token ->
                seleksiWawancaraViewModel?.loadHasilWawancaraAndUpdateStatus(token)
            }
        }

        DialogPilihPeserta(
            pesertaDiterima = pesertaDiterimaFiltered,
            pesertaTerpilih = pesertaTerpilih,
            onDismiss = { showDialogPilihPeserta = false },
            onConfirm = { selectedPeserta: List<Peserta> ->
                jadwalViewModel.setPesertaUntukJadwal(jadwal.id, selectedPeserta)

                selectedPeserta.forEach { peserta ->
                    detailViewModel.initPesertaState(peserta)
                }

                authToken?.let { token ->
                    jadwalViewModel.setAuthToken(token)

                    jadwalViewModel.loadPesertaFromJadwal(jadwal.id)
                }
                showDialogPilihPeserta = false
            },
            colorScheme = colorScheme
        )
    }
}

@Composable
fun PesertaCardWawancara(
    peserta: Peserta,
    pesertaState: PesertaWawancaraState?,
    colorScheme: ColorScheme,
    detailViewModel: DetailJadwalWawancaraViewModel,
    authToken: String?,
    jadwalViewModel: JadwalViewModel,
    jadwalId: Int,
    seleksiWawancaraViewModel: SeleksiWawancaraViewModel? = null,
    onTerimaTolak: () -> Unit = {},
    onHapus: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null
) {
    var showTimeDialog by remember { mutableStateOf(false) }
    var showDivisionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isTimerRunning by remember(peserta.id, peserta.nama) { mutableStateOf(false) }
    var remainingSeconds by remember(peserta.id, peserta.nama) { mutableIntStateOf(0) }
    var timerDuration by remember(peserta.id, peserta.nama) { mutableIntStateOf(0) }

    var hasWarned5Minutes by remember(peserta.id, peserta.nama) { mutableStateOf(false) }

    LaunchedEffect(peserta.id, peserta.nama) {
        detailViewModel.initPesertaState(peserta)
    }

    val timerTick by detailViewModel.timerTick.collectAsState()

    val currentState = detailViewModel.getPesertaState(peserta)
    val statusWawancara = peserta.statusWawancara?.lowercase()?.trim() ?: "pending"

    val hasilWawancara = seleksiWawancaraViewModel?.getHasilWawancaraByPesertaId(peserta.id ?: -1)

    val finalStatus = remember(timerTick, statusWawancara, hasilWawancara?.status, currentState?.status) {
        when {
            statusWawancara == "diterima" -> InterviewStatus.ACCEPTED
            statusWawancara == "ditolak" -> InterviewStatus.REJECTED
            hasilWawancara?.status == "diterima" -> InterviewStatus.ACCEPTED
            hasilWawancara?.status == "ditolak" -> InterviewStatus.REJECTED
            currentState?.status == InterviewStatus.ACCEPTED -> InterviewStatus.ACCEPTED
            currentState?.status == InterviewStatus.REJECTED -> InterviewStatus.REJECTED
            else -> InterviewStatus.PENDING
        }
    }

    val divisi = remember(timerTick, hasilWawancara?.divisi, currentState?.divisi) {
        hasilWawancara?.divisi ?: currentState?.divisi ?: ""
    }
    val alasan = remember(timerTick, hasilWawancara?.alasan, currentState?.alasan) {
        hasilWawancara?.alasan ?: currentState?.alasan ?: ""
    }

    val updatedState = remember(timerTick, currentState, finalStatus, divisi, alasan) {
        currentState?.copy(
            status = finalStatus,
            divisi = divisi,
            alasan = alasan
        ) ?: PesertaWawancaraState(
            pesertaId = peserta.id,
            nama = peserta.nama,
            status = finalStatus,
            divisi = divisi,
            alasan = alasan
        )
    }

    val isFinalStatus = finalStatus == InterviewStatus.ACCEPTED || finalStatus == InterviewStatus.REJECTED

    LaunchedEffect(isTimerRunning) {
        if (!isTimerRunning) return@LaunchedEffect

        var currentSeconds = remainingSeconds

        while (currentSeconds > 0 && isTimerRunning) {
            delay(1000)
            currentSeconds--
            remainingSeconds = currentSeconds
        }

        if (currentSeconds <= 0) {
            isTimerRunning = false
        }
    }

    LaunchedEffect(remainingSeconds, isTimerRunning, hasWarned5Minutes) {
        val fiveMinutesInSeconds = 5 * 60
        val fiveMinutes = 5

        if (isTimerRunning && remainingSeconds <= fiveMinutesInSeconds && remainingSeconds > 0 && !hasWarned5Minutes && timerDuration > fiveMinutes) {
            hasWarned5Minutes = true

            val minutesLeft = remainingSeconds / 60
            val secondsLeft = remainingSeconds % 60
            val timeLeftText = if (minutesLeft > 0) {
                "$minutesLeft menit $secondsLeft detik"
            } else {
                "$secondsLeft detik"
            }

            triggerWarningVibration(context)

            InterviewNotificationHelper.showWarningNotification(
                context = context,
                participantName = peserta.nama,
                scheduleLabel = "Sisa waktu $timeLeftText"
            )

            snackbarHostState?.let { hostState ->
                scope.launch {
                    hostState.showSnackbar(
                        message = "⚠️ Wawancara ${peserta.nama} tersisa $timeLeftText lagi!",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    LaunchedEffect(timerDuration) {
        if (timerDuration > 0) {
            hasWarned5Minutes = false
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val remainingTime = String.format("%02d:%02d", minutes, seconds)

    val isSavingHasil by detailViewModel.isSavingHasil.collectAsState()

    val saveHasilSuccess by detailViewModel.saveHasilSuccess.collectAsState()
    val saveHasilError by detailViewModel.saveHasilError.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                }

                if (onHapus != null) {
                    IconButton(
                        onClick = onHapus,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Color(0xFFD32F2F).copy(alpha = 0.1f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus Peserta",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (!isFinalStatus && updatedState.status == InterviewStatus.PENDING) {
                if (timerDuration > 0 || remainingSeconds > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isTimerRunning) Color(0xFF4CAF50).copy(alpha = 0.1f)
                                        else Color(0xFFFF9800).copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = remainingTime,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTimerRunning) Color(0xFF4CAF50) else Color(
                                        0xFFFF9800
                                    )
                                )
                            }
                            Text(
                                text = when {
                                    isTimerRunning && remainingSeconds > 0 -> "Sedang berlangsung"
                                    remainingSeconds == 0 && timerDuration > 0 -> "Selesai"
                                    !isTimerRunning && remainingSeconds > 0 -> "Dijeda"
                                    else -> "Belum dimulai"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface
                            )
                        }

                        if (remainingSeconds > 0) {
                            if (isTimerRunning) {
                                IconButton(
                                    onClick = { isTimerRunning = false },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Pause Timer",
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { isTimerRunning = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Resume Timer",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                } else {
                    Button(
                        onClick = {
                            detailViewModel.initPesertaState(peserta)
                            showTimeDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSavingHasil && !isFinalStatus
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Mulai Wawancara")
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            detailViewModel.initPesertaState(peserta)
                            detailViewModel.rejectPeserta(peserta, authToken)
                            onTerimaTolak()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            disabledContainerColor = Color(0xFFD32F2F).copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSavingHasil && authToken != null && !isFinalStatus
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Tolak")
                    }

                    Button(
                        onClick = {
                            detailViewModel.initPesertaState(peserta)
                            showDivisionDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSavingHasil && authToken != null && !isFinalStatus
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Terima")
                    }
                }
            } else if (updatedState.status == InterviewStatus.ACCEPTED) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✓ Diterima",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            if (updatedState.divisi.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Divisi: ${updatedState.divisi}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4CAF50).copy(alpha = 0.9f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else if (updatedState.status == InterviewStatus.REJECTED) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✗ Tidak Lulus",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            if (updatedState.alasan.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = updatedState.alasan,
                                    fontSize = 13.sp,
                                    color = Color(0xFFD32F2F).copy(alpha = 0.9f),
                                    lineHeight = 18.sp
                                )
                            } else {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Tidak ada alasan",
                                    fontSize = 13.sp,
                                    color = Color(0xFFD32F2F).copy(alpha = 0.7f),
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }


            if (showTimeDialog) {
                DialogPilihWaktu(
                    onDismiss = { showTimeDialog = false },
                    onConfirm = { minutes: Int ->

                        timerDuration = minutes
                        remainingSeconds = minutes * 60
                        isTimerRunning = true
                        showTimeDialog = false
                    },
                    colorScheme = colorScheme
                )
            }


            if (showDivisionDialog) {
                DialogPilihDivisi(
                    onDismiss = { showDivisionDialog = false },
                    onConfirm = { divisi: String ->

                        detailViewModel.initPesertaState(peserta)
                        detailViewModel.acceptPeserta(peserta, divisi, authToken)

                        onTerimaTolak()
                        showDivisionDialog = false
                    },
                    colorScheme = colorScheme
                )
            }
        }
    }
}


private fun triggerWarningVibration(context: android.content.Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(VibratorManager::class.java)
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    vibrator ?: return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val pattern = longArrayOf(0, 400, 120, 400)
        val amplitudes = intArrayOf(
            0,
            VibrationEffect.DEFAULT_AMPLITUDE,
            0,
            VibrationEffect.DEFAULT_AMPLITUDE
        )
        val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(600L)
    }
}
