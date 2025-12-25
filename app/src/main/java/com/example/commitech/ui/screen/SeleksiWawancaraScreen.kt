package com.example.commitech.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.commitech.notification.InterviewNotificationHelper
import com.example.commitech.ui.components.CircleIconButton
import com.example.commitech.ui.viewmodel.AuthViewModel
import com.example.commitech.ui.viewmodel.DayData
import com.example.commitech.ui.viewmodel.InterviewStatus
import com.example.commitech.ui.viewmodel.ParticipantData
import com.example.commitech.ui.viewmodel.SeleksiWawancaraViewModel
import com.example.commitech.ui.viewmodel.SeleksiBerkasViewModel
import com.example.commitech.data.model.HasilWawancaraResponse
import com.example.commitech.data.model.PendaftarResponse
import kotlinx.coroutines.flow.collect
import androidx.compose.runtime.collectAsState
import android.app.TimePickerDialog
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleksiWawancaraScreen(
    viewModel: SeleksiWawancaraViewModel,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    navController: androidx.navigation.NavController? = null,
    jadwalViewModel: com.example.commitech.ui.viewmodel.JadwalViewModel? = null,
    seleksiBerkasViewModel: SeleksiBerkasViewModel? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Jadwal", "Status")

    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val pesertaLulusCount by viewModel.pesertaLulusCount.collectAsState()
    val isLoadingPesertaLulusCount by viewModel.isLoadingPesertaLulusCount.collectAsState()

    val authState by authViewModel.authState.collectAsState()
    val isSavingHasil by viewModel.isSavingHasil.collectAsState()
    val saveHasilError by viewModel.saveHasilError.collectAsState()
    val saveHasilSuccess by viewModel.saveHasilSuccess.collectAsState()
    val isLoadingJadwal by viewModel.isLoadingJadwal.collectAsState()
    val jadwalError by viewModel.jadwalError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        InterviewNotificationHelper.ensureChannels(context)
    }
    
    LaunchedEffect(authState.token) {
        authState.token?.let { token ->
            kotlinx.coroutines.coroutineScope {
                launch { viewModel.loadHasilWawancaraAndUpdateStatus(token) }
                launch {
                    kotlinx.coroutines.delay(100)
                    viewModel.loadJadwalWawancaraFromDatabase(token)
                }
                launch { viewModel.loadPesertaLulusTanpaJadwal(token) }
                launch { viewModel.loadCountPesertaLulus(token) }
                launch {
                    jadwalViewModel?.setAuthToken(token)
                    delay(600)
                    jadwalViewModel?.daftarJadwal?.forEach { jadwal ->
                        jadwalViewModel?.loadPesertaFromJadwal(jadwal.id)
                    }
                }
            }
        }
    }


    LaunchedEffect(saveHasilSuccess) {
        saveHasilSuccess?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSaveHasilSuccess()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Seleksi Wawancara",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total Peserta",
                                fontSize = 14.sp,
                                color = colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${if (isLoadingPesertaLulusCount) "..." else pesertaLulusCount} Peserta",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = colorScheme.background,
                    contentColor = colorScheme.primary,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            Box(
                                Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTab])
                                    .height(4.dp)
                                    .padding(horizontal = 40.dp)
                                    .background(
                                        color = colorScheme.primary,
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            modifier = Modifier.padding(vertical = 12.dp),
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = if (selectedTab == index) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> WawancaraJadwalContent(
                        viewModel = viewModel,
                        authState = authState,
                        jadwalViewModel = jadwalViewModel,
                        navController = navController,
                        seleksiBerkasViewModel = seleksiBerkasViewModel,
                        modifier = Modifier.weight(1f)
                    )
                    1 -> WawancaraStatusContent(
                        viewModel = viewModel,
                        authViewModel = authViewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (isLoadingJadwal) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = colorScheme.primary
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Memuat jadwal wawancara...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (isSavingHasil) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = colorScheme.primary
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Menyimpan hasil wawancara...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            saveHasilError?.let { error ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearSaveHasilError() },
                    title = {
                        Text(
                            text = "Error",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Text(
                            text = error,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.clearSaveHasilError() }
                        ) {
                            Text(
                                text = "OK",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
fun WawancaraJadwalContent(
    viewModel: SeleksiWawancaraViewModel,
    authState: com.example.commitech.ui.viewmodel.AuthState,
    jadwalViewModel: com.example.commitech.ui.viewmodel.JadwalViewModel? = null,
    navController: androidx.navigation.NavController? = null,
    seleksiBerkasViewModel: SeleksiBerkasViewModel? = null,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val daftarJadwal = jadwalViewModel?.daftarJadwal ?: emptyList()
    val pesertaLulusTanpaJadwal = viewModel.pesertaLulusTanpaJadwal
    val isLoadingPesertaLulus by viewModel.isLoadingPesertaLulus.collectAsState()

    LaunchedEffect(authState.token, daftarJadwal) {
        authState.token?.let { token ->
            if (daftarJadwal.isNotEmpty()) {
                daftarJadwal.forEachIndexed { index, jadwal ->
                    kotlinx.coroutines.delay(index * 100L)
                    jadwalViewModel?.loadPesertaFromJadwal(jadwal.id)
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (daftarJadwal.isNotEmpty()) {
            item {
                Text(
                    text = "Jadwal Wawancara",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(daftarJadwal) { jadwal ->
                JadwalRekrutmenCard(
                    jadwal = jadwal,
                    jadwalViewModel = jadwalViewModel,
                    viewModel = viewModel,
                    authState = authState,
                    pesertaLulusTanpaJadwal = emptyList(),
                    onClick = {
                        navController?.navigate("detailJadwalWawancara/${jadwal.id}")
                    }
                )
            }

        }
    }
}

@Composable
fun PesertaTanpaJadwalCard(
    peserta: com.example.commitech.data.model.PendaftarResponse,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peserta.nama ?: "Nama tidak diketahui",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                if (!peserta.nim.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NIM: ${peserta.nim}",
                        fontSize = 13.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Lulus Berkas",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun JadwalRekrutmenCard(
    jadwal: com.example.commitech.ui.viewmodel.Jadwal,
    jadwalViewModel: com.example.commitech.ui.viewmodel.JadwalViewModel?,
    viewModel: SeleksiWawancaraViewModel,
    authState: com.example.commitech.ui.viewmodel.AuthState,
    pesertaLulusTanpaJadwal: List<com.example.commitech.data.model.PendaftarResponse>,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val jumlahPeserta = jadwalViewModel?.getPesertaByJadwalId(jadwal.id)?.size ?: 0
    val isJadwalFull = jumlahPeserta >= 5

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = jadwal.judul,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
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
                            color = colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Lokasi: ${jadwal.lokasi.ifBlank { "-" }}",
                        fontSize = 13.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${jadwal.tanggalMulai} - ${jadwal.tanggalSelesai}",
                        fontSize = 13.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${jadwal.waktuMulai} - ${jadwal.waktuSelesai}",
                        fontSize = 13.sp,
                        color = Color(0xFF9C27B0),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isJadwalFull) Color(0xFFD32F2F).copy(alpha = 0.1f) else colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "$jumlahPeserta/5 peserta",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isJadwalFull) Color(0xFFD32F2F) else colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Detail",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun WawancaraStatusContent(
    viewModel: SeleksiWawancaraViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var filterStatus by remember { mutableStateOf<InterviewStatus?>(null) }
    val authState by authViewModel.authState.collectAsState()
    
    LaunchedEffect(Unit) {
        authState.token?.let { token ->
            viewModel.loadHasilWawancaraAndUpdateStatus(token)
            viewModel.loadPesertaPendingWawancara(token)
        }
    }

    LaunchedEffect(authState.token, filterStatus) {
        if (filterStatus == InterviewStatus.PENDING || filterStatus == null) {
            authState.token?.let { token -> viewModel.loadPesertaPendingWawancara(token) }
        }
    }

    val hasilWawancaraList by viewModel.hasilWawancaraList.collectAsState()
    val pesertaPendingWawancara by viewModel.pesertaPendingWawancara.collectAsState()
    val isLoadingPesertaPendingWawancara by viewModel.isLoadingPesertaPendingWawancara.collectAsState()

    val filteredList = remember(hasilWawancaraList, filterStatus) {
        viewModel.getHasilWawancaraByStatus(filterStatus).sortedBy { it.namaPeserta }
    }

    val pendingList = remember(pesertaPendingWawancara, hasilWawancaraList) {
        val hasilWawancaraPesertaIds = hasilWawancaraList.map { it.pesertaId }.toSet()
        val filteredPending = if (filterStatus == null) {
            pesertaPendingWawancara.filter { it.id !in hasilWawancaraPesertaIds }
        } else {
            pesertaPendingWawancara
        }
        filteredPending.sortedBy { it.nama ?: "" }
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip("Semua", null, filterStatus) { filterStatus = it }
            FilterChip("Pending", InterviewStatus.PENDING, filterStatus) { filterStatus = it }
            FilterChip("Diterima", InterviewStatus.ACCEPTED, filterStatus) { filterStatus = it }
            FilterChip("Ditolak", InterviewStatus.REJECTED, filterStatus) { filterStatus = it }
        }

        Spacer(Modifier.height(12.dp))

        val showPendingList = filterStatus == InterviewStatus.PENDING
        val showAllList = filterStatus == null

        if ((showPendingList || showAllList) && isLoadingPesertaPendingWawancara) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (showPendingList && pendingList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "Belum ada peserta pending wawancara",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (!showPendingList && !showAllList && filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "Tidak ada data dengan status ${when(filterStatus) {
                            InterviewStatus.ACCEPTED -> "Diterima"
                            InterviewStatus.REJECTED -> "Ditolak"
                            else -> ""
                        }}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (showAllList) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(filteredList) { index, hasil ->
                    StatusRowFromHasilWawancara(no = index + 1, hasilWawancara = hasil)
                }
                
                itemsIndexed(pendingList) { index, peserta ->
                    StatusRowFromPesertaPending(no = filteredList.size + index + 1, peserta = peserta)
                }
                
                if (filteredList.isEmpty() && pendingList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text(
                                    text = "Belum ada data hasil wawancara",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        } else if (showPendingList) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(pendingList) { index, peserta ->
                    StatusRowFromPesertaPending(
                        no = index + 1,
                        peserta = peserta
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(filteredList) { index, hasil ->
                    StatusRowFromHasilWawancara(
                        no = index + 1, 
                        hasilWawancara = hasil
                    )
                }
            }
        }
    }
}

@Composable
fun StatusRowFromPesertaPending(
    no: Int,
    peserta: PendaftarResponse
) {
    val colorScheme = MaterialTheme.colorScheme
    val statusColor = Color(0xFFFF9800)

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$no",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colorScheme.primary
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                peserta.nama ?: "Nama tidak diketahui",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = colorScheme.onSurface
            )

            Spacer(Modifier.width(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    "Pending",
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    status: InterviewStatus?,
    selectedStatus: InterviewStatus?,
    onSelect: (InterviewStatus?) -> Unit
) {
    val selected = selectedStatus == status
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) colorScheme.primary else colorScheme.surface,
        shadowElevation = if (selected) 4.dp else 2.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onSelect(status) }
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun StatusRow(no: Int, name: String, status: InterviewStatus) {
    val colorScheme = MaterialTheme.colorScheme
    val statusColor = when (status) {
        InterviewStatus.ACCEPTED -> Color(0xFF4CAF50)
        InterviewStatus.REJECTED -> Color(0xFFD32F2F)
        else -> Color(0xFFFF9800)
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$no",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colorScheme.primary
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                name,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = colorScheme.onSurface
            )

            Spacer(Modifier.width(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    when (status) {
                        InterviewStatus.ACCEPTED -> "Diterima"
                        InterviewStatus.REJECTED -> "Ditolak"
                        else -> "Pending"
                    },
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun StatusRowFromHasilWawancara(
    no: Int,
    hasilWawancara: HasilWawancaraResponse
) {
    val colorScheme = MaterialTheme.colorScheme
    val status = when (hasilWawancara.status) {
        "diterima" -> InterviewStatus.ACCEPTED
        "ditolak" -> InterviewStatus.REJECTED
        else -> InterviewStatus.PENDING
    }
    val statusColor = when (status) {
        InterviewStatus.ACCEPTED -> Color(0xFF4CAF50)
        InterviewStatus.REJECTED -> Color(0xFFD32F2F)
        else -> Color(0xFFFF9800)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$no",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colorScheme.primary
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        hasilWawancara.namaPeserta,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = colorScheme.onSurface
                    )
                    
                    if (hasilWawancara.tanggalJadwal != null && hasilWawancara.tanggalJadwal != "-") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${hasilWawancara.tanggalJadwal}${if (hasilWawancara.waktuJadwal != null && hasilWawancara.waktuJadwal != "-") " • ${hasilWawancara.waktuJadwal}" else ""}",
                            fontSize = 12.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        when (status) {
                            InterviewStatus.ACCEPTED -> "Diterima"
                            InterviewStatus.REJECTED -> "Ditolak"
                            else -> "Pending"
                        },
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            if (status == InterviewStatus.ACCEPTED && !hasilWawancara.divisi.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Divisi: ${hasilWawancara.divisi}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            if (status == InterviewStatus.REJECTED && !hasilWawancara.alasan.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFD32F2F).copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Alasan: ${hasilWawancara.alasan}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableDayCard(
    viewModel: SeleksiWawancaraViewModel,
    dayIndex: Int,
    authState: com.example.commitech.ui.viewmodel.AuthState
) {
    val day = viewModel.days[dayIndex]
    var expanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .clickable(
                    onClick = { expanded = !expanded },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            day.dayName.take(3),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colorScheme.primary
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            "${day.dayName}, ${day.date}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            day.location,
                            fontSize = 13.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (day.participants.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${day.participants.size} peserta",
                                fontSize = 12.sp,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colorScheme.primary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    if (day.participants.isEmpty()) {
                        Text(
                            text = "Tidak ada peserta",
                            color = Color(0xFF737373),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    } else {
                        day.participants.forEachIndexed { pIndex, participant ->
                            ParticipantCard(
                                participant = participant,
                                day = day,
                                dayIndex = dayIndex,
                                participantIndex = pIndex,
                                viewModel = viewModel,
                                token = authState.token
                            )
                            if (pIndex != day.participants.lastIndex) {
                                HorizontalDivider(thickness = 1.dp, color = Color(0xFFEBEBEB))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantCard(
    participant: ParticipantData,
    day: DayData,
    dayIndex: Int,
    participantIndex: Int,
    viewModel: SeleksiWawancaraViewModel,
    token: String?
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showAcceptDialog by remember { mutableStateOf(false) }
    val isPending = participant.status == InterviewStatus.PENDING
    val canModifyResult = isPending && participant.hasCompleted
    val canEditSchedule = !participant.hasStarted

    val animatedBorderColor by animateColorAsState(
        targetValue = when (participant.status) {
            InterviewStatus.ACCEPTED -> Color(0xFF4CAF50)
            InterviewStatus.REJECTED -> Color(0xFFF44336)
            else -> Color(0xFFE0E0E0)
        },
        label = "borderAnim"
    )
    val animatedShadow by animateDpAsState(
        targetValue = if (participant.status != InterviewStatus.PENDING) 6.dp else 2.dp,
        label = "shadowAnim"
    )

    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(animatedShadow, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        border = BorderStroke(2.dp, animatedBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            participant.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )

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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            participant.time,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        if (participant.status != InterviewStatus.PENDING) {
                            Text("•", color = colorScheme.onSurface.copy(alpha = 0.3f))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (participant.status) {
                                    InterviewStatus.ACCEPTED -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                    InterviewStatus.REJECTED -> Color(0xFFD32F2F).copy(alpha = 0.15f)
                                    else -> Color.Transparent
                                }
                            ) {
                                Text(
                                    when (participant.status) {
                                        InterviewStatus.ACCEPTED -> "✓ Diterima"
                                        InterviewStatus.REJECTED -> "✗ Ditolak"
                                        else -> ""
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (participant.status) {
                                        InterviewStatus.ACCEPTED -> Color(0xFF4CAF50)
                                        InterviewStatus.REJECTED -> Color(0xFFD32F2F)
                                        else -> colorScheme.onSurface
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                if (participant.status == InterviewStatus.ACCEPTED && participant.division.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Divisi: ${participant.division}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                } else if (participant.status == InterviewStatus.REJECTED && participant.reason.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFD32F2F).copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Tidak Lulus",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = participant.reason,
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F).copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircleIconButton(
                        icon = Icons.Default.Check,
                        background = if (participant.status == InterviewStatus.ACCEPTED) Color(0xFF4CAF50) else Color(0xFFE8F5E9),
                        tint = if (participant.status == InterviewStatus.ACCEPTED) Color.White else Color(0xFF1B5E20),
                        enabled = canModifyResult
                    ) {
                        if (canModifyResult) showAcceptDialog = true
                    }

                    CircleIconButton(
                        icon = Icons.Default.Close,
                        background = if (participant.status == InterviewStatus.REJECTED) Color(0xFFD32F2F) else Color(0xFFFFEBEE),
                        tint = if (participant.status == InterviewStatus.REJECTED) Color.White else Color(0xFFB71C1C),
                        enabled = canModifyResult
                    ) {
                        if (canModifyResult) showRejectDialog = true
                    }

                    CircleIconButton(
                        icon = Icons.Default.Edit,
                        background = Color(0xFFF3E5F5),
                        tint = Color(0xFF4A148C),
                        enabled = canEditSchedule
                    ) {
                        if (canEditSchedule) showEditDialog = true
                    }
                }
            }

            if (showRejectDialog) {
                RejectDialog(
                    onDismiss = { showRejectDialog = false },
                    onConfirm = { reason ->
                        viewModel.rejectWithReason(
                            dayIndex = dayIndex,
                            index = participantIndex,
                            reason = reason,
                            token = token
                        )
                        showRejectDialog = false
                    }
                )
            }

            if (showAcceptDialog) {
                AcceptDialog(
                    onDismiss = { showAcceptDialog = false },
                    onConfirm = { division ->
                        viewModel.acceptWithDivision(
                            dayIndex = dayIndex,
                            index = participantIndex,
                            division = division,
                            token = token
                        )
                        showAcceptDialog = false
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            InterviewTimerControls(
                participant = participant,
                isPending = isPending,
                onStart = { viewModel.startInterview(dayIndex, participantIndex) },
                onStop = { viewModel.stopInterview(dayIndex, participantIndex) }
            )
        }
    }

    if (showInfoDialog) {
        ParticipantInfoDialog(
            participant = participant,
            onDismiss = { showInfoDialog = false }
        )
    }

    if (showEditDialog) {
        EditScheduleDialog(
            participant = participant,
            day = day,
            availableDates = viewModel.days.map { it.date },
            onDismiss = { showEditDialog = false },
            onSave = { newDate, newTime, newLocation ->
                val ok = viewModel.moveOrUpdateParticipantSchedule(dayIndex, participantIndex, newDate, newTime, newLocation)
                if (ok) {
                    showEditDialog = false
                }
            }
        )
    }
}

@Composable
fun InterviewTimerControls(
    participant: ParticipantData,
    isPending: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val remainingLabel = formatRemainingTime(participant.remainingSeconds)
    val showStartButton = isPending && !participant.hasStarted
    val showStopButton = participant.isOngoing
    val showAwaitingDecision = isPending && participant.hasCompleted

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            when {
                participant.isOngoing -> {
                    Text(
                        text = "Sedang berlangsung",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Sisa waktu $remainingLabel",
                        fontSize = 12.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                showAwaitingDecision -> {
                    Text(
                        text = "Sesi wawancara selesai",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Silakan tentukan keputusan peserta",
                        fontSize = 12.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                else -> {
                    Text(
                        text = "Durasi sesi",
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "${participant.durationMinutes} menit",
                        fontSize = 12.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        when {
            showStopButton -> {
                OutlinedButton(
                    onClick = onStop,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Selesai")
                }
            }

            showStartButton -> {
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mulai")
                }
            }
        }
    }
}

@Composable
fun ParticipantInfoDialog(
    participant: ParticipantData,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    participant.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1A1A40)
                )

                Spacer(Modifier.height(16.dp))

                Text("Status Wawancara", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(
                    text = when (participant.status) {
                        InterviewStatus.ACCEPTED -> "Diterima"
                        InterviewStatus.REJECTED -> "Ditolak"
                        else -> "Pending"
                    },
                    color = when (participant.status) {
                        InterviewStatus.ACCEPTED -> Color(0xFF2E7D32)
                        InterviewStatus.REJECTED -> Color(0xFFD32F2F)
                        else -> Color.Gray
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Spacer(Modifier.height(16.dp))

                DetailRowSeleksi(label = "Pilihan Divisi 1", value = "Konsumsi")
                DetailRowSeleksi(label = "Alasan Divisi 1", value = "Ingin menjadi bagian divisi konsumsi")

                Spacer(Modifier.height(8.dp))

                DetailRowSeleksi(label = "Pilihan Divisi 2", value = "Acara")
                DetailRowSeleksi(label = "Alasan Divisi 2", value = "Ingin menjadi bagian divisi acara")

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Tutup")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleDialog(
    participant: ParticipantData,
    day: DayData,
    availableDates: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var newDate by remember { mutableStateOf(day.date) }
    var newTime by remember { mutableStateOf(participant.time) }
    var newLocation by remember { mutableStateOf(day.location) }

    val context = LocalContext.current
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))
    }
    var showDatePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            color = Color.White
        ) {
            Column(Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = Color(0xFF2196F3).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    participant.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1A1A40),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    "Edit jadwal wawancara",
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "Tanggal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A40)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newDate,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Event, contentDescription = "Pilih tanggal", tint = Color(0xFF1A73E8))
                        }
                    }
                )
                if (showDatePicker) {
                    val initialMillis = runCatching {
                        LocalDate.parse(newDate, dateFormatter)
                            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    }.getOrNull()
                    val allowedSet = remember(availableDates) { availableDates.toSet() }
                    val pickerState = rememberDatePickerState(
                        initialSelectedDateMillis = initialMillis,
                        selectableDates = object : androidx.compose.material3.SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                val ld = Instant.ofEpochMilli(utcTimeMillis)
                                    .atZone(ZoneId.systemDefault()).toLocalDate()
                                val label = ld.format(dateFormatter)
                                return allowedSet.contains(label)
                            }
                        }
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val millis = pickerState.selectedDateMillis
                                    if (millis != null) {
                                        val ld = Instant.ofEpochMilli(millis)
                                            .atZone(ZoneId.systemDefault()).toLocalDate()
                                        newDate = ld.format(dateFormatter)
                                    }
                                    showDatePicker = false
                                }
                            ) { Text("Pilih") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
                        }
                    ) {
                        DatePicker(state = pickerState)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Waktu",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A40)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newTime,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            val (initialHour, initialMinute) = runCatching {
                                val parts = newTime.replace(" WIB", "").split(".")
                                parts[0].toInt() to parts[1].toInt()
                            }.getOrElse { cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE) }

                            TimePickerDialog(
                                context,
                                { _, hour: Int, minute: Int ->
                                    newTime = String.format(Locale.getDefault(), "%02d.%02d WIB", hour, minute)
                                },
                                initialHour,
                                initialMinute,
                                true
                            ).show()
                        }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Pilih waktu", tint = Color(0xFF1A73E8))
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Tempat",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A40)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newLocation,
                    onValueChange = { newLocation = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onSave(newDate, newTime, newLocation) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF2196F3))
                    ) {
                        Text(
                            "Tutup",
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditableRow(label: String, value: String, edit: Boolean, onChange: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        if (edit) {
            TextField(value, onChange, singleLine = true, modifier = Modifier.width(150.dp))
        } else {
            Text(value)
        }
    }
}

@Composable
fun DetailRowSeleksi(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Spacer(Modifier.width(12.dp))
        Text(value)
    }
}

@Composable
fun RejectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            color = Color.White
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color(0xFFEF5350).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "Anda Yakin Untuk Menolak\nPeserta ini?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1A1A40),
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Text(
                    "Keputusan ini tidak dapat dibatalkan",
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "Alasan Penolakan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A40),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = {
                        Text(
                            "Tuliskan alasan penolakan...",
                            fontSize = 14.sp,
                            color = Color(0xFFBDBDBD)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFE0E0E0))
                    ) {
                        Text(
                            "Batal",
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { if (reason.isNotBlank()) onConfirm(reason) },
                        modifier = Modifier.weight(1f),
                        enabled = reason.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF5350),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Tolak", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AcceptDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedDivision by remember { mutableStateOf("") }
    val divisions = listOf(
        "Acara" to Icons.Default.Event,
        "Humas" to Icons.Default.Campaign,
        "Konsumsi" to Icons.Default.Restaurant,
        "Perlengkapan" to Icons.Default.Inventory
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            color = Color.White
        ) {
            Column(
                Modifier.padding(24.dp),
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
                    color = Color(0xFF1A1A40)
                )

                Text(
                    "Peserta akan ditempatkan di divisi ini",
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(24.dp))

                divisions.forEach { (division, icon) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { selectedDivision = division },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedDivision == division)
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else
                                Color(0xFFF5F5F5)
                        ),
                        border = BorderStroke(
                            width = 2.dp,
                            color = if (selectedDivision == division)
                                Color(0xFF4CAF50)
                            else
                                Color.Transparent
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = if (selectedDivision == division)
                                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                                        else
                                            Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (selectedDivision == division)
                                        Color(0xFF4CAF50)
                                    else
                                        Color(0xFF666666),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                division,
                                fontSize = 16.sp,
                                fontWeight = if (selectedDivision == division)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal,
                                color = if (selectedDivision == division)
                                    Color(0xFF1A1A40)
                                else
                                    Color(0xFF666666),
                                modifier = Modifier.weight(1f)
                            )

                            RadioButton(
                                selected = selectedDivision == division,
                                onClick = { selectedDivision = division },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF4CAF50),
                                    unselectedColor = Color(0xFFBDBDBD)
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFE0E0E0))
                    ) {
                        Text(
                            "Batal",
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { if (selectedDivision.isNotBlank()) onConfirm(selectedDivision) },
                        modifier = Modifier.weight(1f),
                        enabled = selectedDivision.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Terima",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatRemainingTime(totalSeconds: Int): String {
    val clamped = totalSeconds.coerceAtLeast(0)
    val minutes = clamped / 60
    val seconds = clamped % 60
    return "%02d:%02d".format(minutes, seconds)
}

