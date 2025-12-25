package com.example.commitech.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import com.example.commitech.R
import com.example.commitech.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLanding: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000)
    )

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        visible = true
        delay(1500)
        

        while (authState.isLoading) {
            delay(100)
        }
        

        if (authState.isAuthenticated && authState.token != null) {
            onNavigateToHome()
        } else {
            onNavigateToLanding()
        }
    }


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
            Image(
                painter = painterResource(id = R.drawable.commitechlogo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(500.dp)
                    .alpha(alpha)
            )

            Text(
                text = "COMMITECH",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}
