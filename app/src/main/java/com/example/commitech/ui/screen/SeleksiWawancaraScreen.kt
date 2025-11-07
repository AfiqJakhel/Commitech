package com.example.commitech.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.commitech.ui.viewmodel.DayData
import com.example.commitech.ui.viewmodel.InterviewStatus
import com.example.commitech.ui.viewmodel.ParticipantData
import com.example.commitech.ui.viewmodel.SeleksiWawancaraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleksiWawancaraScreen(
    viewModel: SeleksiWawancaraViewModel,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Jadwal", "Status")

    val colorScheme = MaterialTheme.colorScheme
    val totalPeserta = viewModel.totalParticipants()
    
    Scaffold(
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Header Card dengan Total Peserta
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
                            "$totalPeserta Peserta",
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
            
            // Modern Tabs
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
                    modifier = Modifier.weight(1f)
                )
                1 -> WawancaraStatusContent(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun WawancaraJadwalContent(
    viewModel: SeleksiWawancaraViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(viewModel.days.size) { index ->
            ExpandableDayCard(viewModel, index)
        }
    }
}

@Composable
fun WawancaraStatusContent(
    viewModel: SeleksiWawancaraViewModel,
    modifier: Modifier = Modifier
) {
    var filterStatus by remember { mutableStateOf<InterviewStatus?>(null) }
    val participants = viewModel.getAllParticipants()
    val filteredList = remember(participants, filterStatus) {
        if (filterStatus == null) participants else participants.filter { it.status == filterStatus }
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {

        // Filter chips
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(filteredList) { index, p ->
                StatusRow(no = index + 1, name = p.name, status = p.status)
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
            // Number Badge
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
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Name
            Text(
                name,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Status Badge
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
fun ExpandableDayCard(viewModel: SeleksiWawancaraViewModel, dayIndex: Int) {
    val day = viewModel.days[dayIndex]
    var expanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .clickable(
                    onClick = { expanded = !expanded },
                    indication = LocalIndication.current,
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
                    // Day Icon
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
                                viewModel = viewModel
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
    viewModel: SeleksiWawancaraViewModel
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showAcceptDialog by remember { mutableStateOf(false) }
    val isDone = participant.status != InterviewStatus.PENDING

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        participant.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Time
                        Text(
                            participant.time,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        // Status
                        if (participant.status != InterviewStatus.PENDING) {
                            Text("•", color = colorScheme.onSurface.copy(alpha = 0.3f))
                            Text(
                                when (participant.status) {
                                    InterviewStatus.ACCEPTED -> "✓ Diterima"
                                    InterviewStatus.REJECTED -> "✗ Ditolak"
                                    else -> ""
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (participant.status) {
                                    InterviewStatus.ACCEPTED -> Color(0xFF4CAF50)
                                    InterviewStatus.REJECTED -> Color(0xFFD32F2F)
                                    else -> colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
                
                // Info Button
                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                // Tombol Terima
                CircleIconButton(
                    icon = Icons.Default.Check,
                    background = if (participant.status == InterviewStatus.ACCEPTED) Color(0xFF4CAF50) else Color(0xFFE8F5E9),
                    tint = if (participant.status == InterviewStatus.ACCEPTED) Color.White else Color(0xFF1B5E20),
                    enabled = !isDone
                ) {
                    if (!isDone) showAcceptDialog = true
                }

                // Tombol Tolak
                CircleIconButton(
                    icon = Icons.Default.Close,
                    background = if (participant.status == InterviewStatus.REJECTED) Color(0xFFD32F2F) else Color(0xFFFFEBEE),
                    tint = if (participant.status == InterviewStatus.REJECTED) Color.White else Color(0xFFB71C1C),
                    enabled = !isDone
                ) {
                    if (!isDone) showRejectDialog = true
                }

                // Tampilkan Dialog
                if (showRejectDialog) {
                    RejectDialog(
                        onDismiss = { showRejectDialog = false },
                        onConfirm = { reason ->
                            viewModel.rejectWithReason(dayIndex, participantIndex, reason)
                            showRejectDialog = false
                        }
                    )
                }

                if (showAcceptDialog) {
                    AcceptDialog(
                        onDismiss = { showAcceptDialog = false },
                        onConfirm = { division ->
                            viewModel.acceptWithDivision(dayIndex, participantIndex, division)
                            showAcceptDialog = false
                        }
                    )
                }

                CircleIconButton(
                    icon = Icons.Default.Edit,
                    background = Color(0xFFF3E5F5),
                    tint = Color(0xFF4A148C),
                    enabled = !isDone
                ) {
                    if (!isDone) showEditDialog = true
                }
            }
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
            onDismiss = { showEditDialog = false },
            onSave = { newDate, newTime, newLocation ->
                viewModel.updateParticipant(dayIndex, participantIndex, newDate, newTime, newLocation)
                showEditDialog = false
            }
        )
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

                // Status ditampilkan sebagai teks saja (sesuai permintaan)
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

@Composable
fun EditScheduleDialog(
    participant: ParticipantData,
    day: DayData,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var newDate by remember { mutableStateOf(day.date) }
    var newTime by remember { mutableStateOf(participant.time) }
    var newLocation by remember { mutableStateOf(day.location) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp)) {

                Text(participant.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))

                EditableRow("Tanggal", newDate, true) { newDate = it }
                EditableRow("Waktu", newTime, true) { newTime = it }
                EditableRow("Tempat", newLocation, true) { newLocation = it }

                Spacer(Modifier.height(24.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onSave(newDate, newTime, newLocation) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Simpan")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("Tutup")
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
fun CircleIconButton(
    icon: ImageVector,
    background: Color,
    tint: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = if (enabled) background else Color.LightGray.copy(alpha = 0.4f),
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(50.dp))
            .clickable(enabled = enabled) { onClick() }
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) tint else Color.Gray,
            modifier = Modifier.padding(6.dp)
        )
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
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Anda Yakin Untuk Menolak Peserta ini?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A40)
                )

                Spacer(Modifier.height(16.dp))

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(60.dp)
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("Tuliskan alasan penolakan...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { if (reason.isNotBlank()) onConfirm(reason) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Tolak")
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
    val divisions = listOf("Acara", "Humas", "Konsumsi", "Perlengkapan")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Pilih Divisi Penerimaan",
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                divisions.forEach { division ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDivision = division }
                            .padding(6.dp)
                    ) {
                        RadioButton(
                            selected = selectedDivision == division,
                            onClick = { selectedDivision = division }
                        )
                        Text(division)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { if (selectedDivision.isNotBlank()) onConfirm(selectedDivision) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Terima")
                }
            }
        }
    }
}

