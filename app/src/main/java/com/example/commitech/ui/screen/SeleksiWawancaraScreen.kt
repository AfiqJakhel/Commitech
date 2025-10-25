package com.example.commitech.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Seleksi Wawancara", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("${viewModel.totalParticipants()} Peserta", fontSize = 14.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) Color(0xFF1A1A40) else Color.Gray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFF1A1A40) else Color(0xFFE9E9E9),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onSelect(status) }
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF464646),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun StatusRow(no: Int, name: String, status: InterviewStatus) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text("$no.")
            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(name, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Info, contentDescription = "Detail", tint = Color(0xFF2196F3))
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                when (status) {
                    InterviewStatus.ACCEPTED -> "Diterima"
                    InterviewStatus.REJECTED -> "Ditolak"
                    else -> "Pending"
                },
                color = when (status) {
                    InterviewStatus.ACCEPTED -> Color(0xFF2E7D32)
                    InterviewStatus.REJECTED -> Color(0xFFD32F2F)
                    else -> Color.Gray
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ExpandableDayCard(viewModel: SeleksiWawancaraViewModel, dayIndex: Int) {
    val day = viewModel.days[dayIndex]
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "${day.dayName}, ${day.date}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF1A1A40)
                    )
                    Text(
                        day.location,
                        fontSize = 13.sp,
                        color = Color(0xFF6E6E6E)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF1A1A40)
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

    Surface(
        shape = RoundedCornerShape(14.dp),
        shadowElevation = animatedShadow,
        border = BorderStroke(2.dp, animatedBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(Modifier.weight(1f)) {
                Text(
                    participant.time,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF005CBB)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        participant.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A40)
                    )
                    Spacer(Modifier.width(8.dp))

                    CircleIconButton(
                        icon = Icons.Default.Info,
                        background = Color(0xFFE3F2FD),
                        tint = Color(0xFF2196F3)
                    ) { showInfoDialog = true }
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
    val divisions = listOf("Konsumsi", "Acara", "Humas", "Perlengkapan")

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

