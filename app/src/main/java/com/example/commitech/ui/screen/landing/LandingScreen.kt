package com.example.commitech.ui.screen.landing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.commitech.R

// ===========================================================
// 🌈 Halaman Landing
// ===========================================================
@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Gambar utama landing page
            Image(
                painter = painterResource(id = R.drawable.landingpage),
                contentDescription = "Landing Illustration",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Judul
            Text(
                text = "COMMITECH",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Tombol Login (Outlined)
            SimpleOutlinedButton(
                text = "LOGIN",
                borderColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.primary,
                onClick = onLoginClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Register (Outlined)
            SimpleOutlinedButton(
                text = "REGISTER",
                borderColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.primary,
                onClick = onRegisterClick
            )
        }
    }
}

// ===========================================================
// 🔘 Reusable Outlined Button (tanpa animasi warna)
// ===========================================================
@Composable
fun SimpleOutlinedButton(
    text: String,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Saat ditekan/hover sedikit ubah warna agar terasa responsif
    val currentBorderColor = when {
        isPressed -> borderColor.copy(alpha = 0.8f)
        isHovered -> borderColor.copy(alpha = 0.9f)
        else -> borderColor
    }

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = textColor
        ),
        border = BorderStroke(1.dp, SolidColor(currentBorderColor)),
        interactionSource = interactionSource
    ) {
        Text(text = text, fontWeight = FontWeight.Medium)
    }
}
