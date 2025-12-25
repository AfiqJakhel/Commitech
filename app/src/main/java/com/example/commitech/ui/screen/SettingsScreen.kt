package com.example.commitech.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.commitech.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    authViewModel: com.example.commitech.ui.viewmodel.AuthViewModel,
    viewModel: SettingsViewModel = viewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val settingsState by viewModel.settingsState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Pengaturan",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 0
                ) {
                    ProfileSection(
                        userName = authState.user?.name ?: "Admin BEM",
                        userEmail = authState.user?.email ?: "admin@bem.ac.id"
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 30
                ) {
                    SettingsSectionHeader(
                        icon = Icons.Default.Palette,
                        title = "Tampilan"
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 50
                ) {
                    SettingsToggleItem(
                        icon = Icons.Default.DarkMode,
                        title = "Mode Gelap",
                        subtitle = "Aktifkan tema gelap",
                        checked = settingsState.isDarkTheme,
                        onCheckedChange = { viewModel.toggleDarkTheme(it) }
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 70
                ) {
                    SettingsClickableItem(
                        icon = Icons.Default.Language,
                        title = "Bahasa",
                        subtitle = settingsState.selectedLanguage,
                        onClick = { showLanguageDialog = true }
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 90
                ) {
                    SettingsSectionHeader(
                        icon = Icons.Default.Notifications,
                        title = "Notifikasi"
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 110
                ) {
                    SettingsToggleItem(
                        icon = Icons.Default.NotificationsActive,
                        title = "Notifikasi Push",
                        subtitle = "Terima notifikasi penting",
                        checked = settingsState.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 130
                ) {
                    SettingsToggleItem(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Suara",
                        subtitle = "Aktifkan suara notifikasi",
                        checked = settingsState.soundEnabled,
                        onCheckedChange = { viewModel.toggleSound(it) }
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 150
                ) {
                    SettingsSectionHeader(
                        icon = Icons.Default.Storage,
                        title = "Data & Penyimpanan"
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 170
                ) {
                    SettingsToggleItem(
                        icon = Icons.Default.Backup,
                        title = "Backup Otomatis",
                        subtitle = "Cadangkan data secara otomatis",
                        checked = settingsState.autoBackup,
                        onCheckedChange = { viewModel.toggleAutoBackup(it) }
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 190
                ) {
                    SettingsClickableItem(
                        icon = Icons.Default.CloudDownload,
                        title = "Unduh Data",
                        subtitle = "Export data ke perangkat",
                        onClick = { }
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 210
                ) {
                    SettingsClickableItem(
                        icon = Icons.Default.DeleteSweep,
                        title = "Hapus Cache",
                        subtitle = "Bersihkan data sementara",
                        onClick = { }
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 230
                ) {
                    SettingsSectionHeader(
                        icon = Icons.Default.Info,
                        title = "Tentang"
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 250
                ) {
                    SettingsClickableItem(
                        icon = Icons.Default.Description,
                        title = "Syarat & Ketentuan",
                        subtitle = "Baca kebijakan aplikasi",
                        onClick = { }
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 270
                ) {
                    SettingsClickableItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "Kebijakan Privasi",
                        subtitle = "Pelajari tentang privasi Anda",
                        onClick = { }
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 290
                ) {
                    SettingsClickableItem(
                        icon = Icons.Default.Update,
                        title = "Versi Aplikasi",
                        subtitle = "v1.0.0 (Build 1)",
                        onClick = { }
                    )
                }
            }
            
            item {
                AnimatedSettingsCard(
                    isVisible = isVisible,
                    delay = 310
                ) {
                    LogoutButton(onClick = { showLogoutDialog = true })
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = settingsState.selectedLanguage,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { language ->
                viewModel.setLanguage(language)
                showLanguageDialog = false
            }
        )
    }
    
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogoutClick()
            }
        )
    }
}

@Composable
fun AnimatedSettingsCard(
    isVisible: Boolean,
    delay: Int,
    content: @Composable () -> Unit
) {
    var animatedVisibility by remember { mutableStateOf(false) }
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            kotlinx.coroutines.delay(delay.toLong())
            animatedVisibility = true
        }
    }
    
    AnimatedVisibility(
        visible = animatedVisibility,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
            )
        ) + slideInVertically(
            initialOffsetY = { 30 },
            animationSpec = tween(
                durationMillis = 300,
                easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
            )
        )
    ) {
        content()
    }
}

@Composable
fun ProfileSection(
    userName: String = "Admin BEM",
    userEmail: String = "admin@bem.ac.id"
) {
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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    userEmail,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(
    icon: ImageVector,
    title: String
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(checked) {
        scale.animateTo(
            targetValue = 0.95f,
            animationSpec = tween(100)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
            .shadow(2.dp, RoundedCornerShape(16.dp)),
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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colorScheme.primary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE0E0E0)
                )
            )
        }
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val scale = remember { Animatable(1f) }
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    scope.launch {
                        scale.animateTo(0.95f, tween(100))
                        scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
                    }
                    onClick()
                }
            ),
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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    
    Button(
        onClick = {
            scope.launch {
                scale.animateTo(0.95f, tween(100))
                scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
            }
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale.value)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEF5350)
        )
    ) {
        Icon(
            Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "Keluar",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        "Bahasa Indonesia",
        "English",
        "日本語 (Japanese)",
        "한국어 (Korean)"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Pilih Bahasa",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column {
                languages.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == currentLanguage,
                            onClick = { onLanguageSelected(language) }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            language,
                            fontSize = 16.sp,
                            fontWeight = if (language == currentLanguage) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = Color(0xFFEF5350),
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "Keluar dari Aplikasi?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                "Apakah Anda yakin ingin keluar? Anda perlu login kembali untuk mengakses aplikasi.",
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF5350)
                )
            ) {
                Text("Keluar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
