package com.example.commitech.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.commitech.data.model.HasilWawancaraResponse
import com.example.commitech.data.model.PendaftarResponse
import com.example.commitech.notification.InterviewNotificationHelper
import com.example.commitech.ui.viewmodel.AuthViewModel
import com.example.commitech.ui.viewmodel.InterviewStatus
import com.example.commitech.ui.viewmodel.SeleksiWawancaraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleksiWawancaraScreen(
    viewModel: SeleksiWawancaraViewModel,
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    navController: androidx.navigation.NavController? = null,
    jadwalViewModel: com.example.commitech.ui.viewmodel.JadwalViewModel? = null
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

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        InterviewNotificationHelper.ensureChannels(context)
    }
    
    LaunchedEffect(authState.token) {
        authState.token?.let { token ->
            kotlinx.coroutines.coroutineScope {
                launch { viewModel.loadHasilWawancaraAndUpdateStatus(token) }
                launch {
                    delay(100)
                    viewModel.loadJadwalWawancaraFromDatabase(token)
                }
                launch { viewModel.loadPesertaLulusTanpaJadwal(token) }
                launch { viewModel.loadCountPesertaLulus(token) }
                launch {
                    jadwalViewModel?.setAuthToken(token)
                    delay(600)
                    jadwalViewModel?.daftarJadwal?.forEach { jadwal ->
                        jadwalViewModel.loadPesertaFromJadwal(jadwal.id)
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
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val daftarJadwal = jadwalViewModel?.daftarJadwal ?: emptyList()
    viewModel.pesertaLulusTanpaJadwal

    LaunchedEffect(authState.token, daftarJadwal) {
        authState.token?.let { _ ->
            if (daftarJadwal.isNotEmpty()) {
                daftarJadwal.forEachIndexed { index, jadwal ->
                    delay(index * 100L)
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
                    onClick = {
                        navController?.navigate("detailJadwalWawancara/${jadwal.id}")
                    }
                )
            }

        }
    }
}

@Composable
fun JadwalRekrutmenCard(
    jadwal: com.example.commitech.ui.viewmodel.Jadwal,
    jadwalViewModel: com.example.commitech.ui.viewmodel.JadwalViewModel?,
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
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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

