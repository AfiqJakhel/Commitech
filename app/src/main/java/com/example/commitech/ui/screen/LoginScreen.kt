package com.example.commitech.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.commitech.ui.components.ErrorDialog
import com.example.commitech.ui.theme.LocalTheme
import com.example.commitech.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUpClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val authState by authViewModel.authState.collectAsState()
    
    // Navigate to home when login successful
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onLoginClick()
        }
    }
    
    // Show error dialog
    authState.error?.let { errorMessage ->
        ErrorDialog(
            title = "Login Gagal",
            message = errorMessage,
            onDismiss = { authViewModel.clearError() }
        )
    }

    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 64.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // 🔙 HEADER (Back + Title di tengah)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Tombol Back (paling kiri)
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "Back",
                        tint = colorScheme.primary
                    )
                }

                // Judul di tengah

            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // 🧑 Email Field
            Text(
                text = "Email",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp, bottom = 4.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Type your email") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                    cursorColor = colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔒 Password Field
            Text(
                text = "Password",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp, bottom = 4.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Type your password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                    cursorColor = colorScheme.primary
                )
            )

            // 🔗 Forgot password
            Text(
                text = "Forgot password?",
                fontSize = 13.sp,
                color = colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
                    .clickable { onForgotPassword() }
            )

            Spacer(modifier = Modifier.height(28.dp))

            val loginColor = LocalTheme.current.ButtonLogin
            // 🔘 Tombol Login
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        authViewModel.login(email, password)
                    }
                },
                enabled = !authState.isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = loginColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Login",
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight(Alignment.Bottom)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // "Already have an account?" text dengan warna hitam
                    Text(
                        text = "Don't have an account?",
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onBackground,  // Warna hitam
                        fontSize = 14.sp,
                    )

                    // Spasi kecil antara teks
                    Spacer(modifier = Modifier.width(4.dp))


                    // "Sign In" dengan warna utama
                    Text(
                        modifier = Modifier
                            .clickable { onSignUpClick() },
                        text = "Sign Up",
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.primary, // Warna tema (warna utama)
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
