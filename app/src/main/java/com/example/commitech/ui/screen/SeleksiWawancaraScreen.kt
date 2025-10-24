package com.example.commitech.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.commitech.R
import com.example.commitech.ui.viewmodel.DayData
import com.example.commitech.ui.viewmodel.ParticipantData
import com.example.commitech.ui.viewmodel.SeleksiWawancaraViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleksiWawancaraScreen(
    viewModel: SeleksiWawancaraViewModel,
    onBackClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val days = viewModel.days

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Seleksi Wawancara",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorScheme.primary
                        )
                        Text(
                            "4 Peserta",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background
                )
            )
        },
        containerColor = colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(days.size) { index ->
                ExpandableDayCard(day = days[index])
            }
        }
    }
}

@Composable
fun ExpandableDayCard(day: DayData) {
    var expanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    val gradient = Brush.verticalGradient(
        colors = listOf(
            colorScheme.surface,
            colorScheme.surface.copy(alpha = 0.9f)
        )
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(brush = gradient)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    // 🔹 seluruh header jadi area klik
                    onClick = { expanded = !expanded },
                    indication = LocalIndication.current,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .padding(16.dp)
        ) {
            // Header Hari — seluruh bagian ini bisa diklik
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${day.dayName}, ${day.date}",
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = day.location,
                        fontSize = 13.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = colorScheme.primary
                )
            }

            // Isi Peserta (expandable)
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (day.participants.isEmpty()) {
                        Text(
                            text = "Tidak ada peserta wawancara di hari ini",
                            color = colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        day.participants.forEach { participant ->
                            ParticipantCard(participant)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ParticipantCard(participant: ParticipantData) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        color = colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = participant.time,
                    fontSize = 12.sp,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = participant.name,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(onClick = { /* Detail */ }) {
                    Icon(
                        painter = painterResource(R.drawable.icon_info),
                        contentDescription = "Info",
                        tint = Color(0xFF2196F3)
                    )
                }
                IconButton(onClick = { /* Lolos */ }) {
                    Icon(
                        painter = painterResource(R.drawable.icon_check),
                        contentDescription = "Lolos",
                        tint = Color(0xFF4CAF50)
                    )
                }
                IconButton(onClick = { /* Tidak Lolos */ }) {
                    Icon(
                        painter = painterResource(R.drawable.icon_close),
                        contentDescription = "Tidak Lolos",
                        tint = Color(0xFFF44336)
                    )
                }
                IconButton(onClick = { /* Edit */ }) {
                    Icon(
                        painter = painterResource(R.drawable.icon_edit),
                        contentDescription = "Edit",
                        tint = Color(0xFF7B1FA2)
                    )
                }
            }
        }
    }
}