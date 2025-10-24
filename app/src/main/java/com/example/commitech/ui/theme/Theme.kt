package com.example.commitech.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


private val DarkColorScheme = darkColorScheme(
    primary = TealDark,                       // Warna utama dark mode
    onPrimary = TealOn,                       // Teks di atas warna utama
    background = TealDarkBackground,          // Latar belakang umum
    onBackground = TealDarkOnBackground,      // Warna teks di background
    surface = TealSurfaceDark,                // Permukaan konten
    onSurface = TealOnSurfaceDark,            // Warna teks di permukaan
    secondary = BlueDarkSecondaryContainer,   // Warna sekunder (aksen biru)
    onSecondary = BlueDarkOnSecondaryContainer,
    primaryContainer = ButtonLoginDarkContainer
)


// ==========================
// 🌞 LIGHT COLOR SCHEME
// ==========================
private val LightColorScheme = lightColorScheme(
    primary = TealLight,                      // Warna utama
    onPrimary = TealOn,                       // Warna teks di atas teal
    background = TealBackground,              // Latar belakang umum
    onBackground = TealOnBackground,          // Warna teks utama
    surface = TealSurface,                    // Permukaan (kartu, tombol)
    onSurface = TealOnSurface,                // Teks di permukaan
    secondary = BlueSecondary,                // Aksen biru
    onSecondary = BlueOnSecondaryContainer,   // Warna teks di atas biru
    primaryContainer = ButtonLoginContainer
)


// ==========================
// 🎨 THEME WRAPPER
// ==========================

data class Theme(
    val ButtonLogin : Color,
    val DataPendaftar: Color,
    val SeleksiBerkas: Color,
    val JadwalWawancara: Color,
    val SeleksiWawancara: Color,
    val PengumumanKelulusan: Color
)
val LocalTheme = staticCompositionLocalOf {
    Theme(
        ButtonLogin = Color.Unspecified,
        DataPendaftar = Color.Unspecified,
        SeleksiBerkas = Color.Unspecified,
        JadwalWawancara = Color.Unspecified,
        SeleksiWawancara = Color.Unspecified,
        PengumumanKelulusan = Color.Unspecified
    )
}

@Composable
fun CommitechTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color hanya untuk Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Tambahkan custom warna login otomatis sesuai tema
    val customColors = if (darkTheme) {
        Theme(
            ButtonLogin = ButtonLoginDarkContainer,
            DataPendaftar = DataPendaftarBlackCard,
            SeleksiBerkas = SeleksiBerkasBlackCard,
            JadwalWawancara = JadwalWawancaraBlackCard,
            SeleksiWawancara = SeleksiWawancaraBlackCard,
            PengumumanKelulusan = PengumumanBlackCard
        )
    } else {
        Theme(
            ButtonLogin = ButtonLoginContainer,
            DataPendaftar = DataPendaftarCard,
            SeleksiBerkas = SeleksiBerkasCard,
            JadwalWawancara = JadwalWawancaraCard,
            SeleksiWawancara = SeleksiWawancaraCard,
            PengumumanKelulusan = PengumumanCard
        )
    }

    CompositionLocalProvider(
        LocalTheme provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}