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
fun SignUpScreen(
    onBackClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    val authState by authViewModel.authState.collectAsState()
    
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onSignUpClick()
        }
    }
    
    authState.error?.let { errorMessage ->
        ErrorDialog(
            title = "Registrasi Gagal",
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
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

            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Name",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp, bottom = 4.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Type your name") },
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

            Text(
                text = "Email",
                fontWeight = FontWeight.ExtraBold,
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

            Text(
                text = "Password",
                fontWeight = FontWeight.ExtraBold,
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Confirm Password",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp, bottom = 4.dp)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirm your password") },
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

            Spacer(modifier = Modifier.height(28.dp))

            val loginColor = LocalTheme.current.ButtonLogin
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && 
                        password.isNotBlank() && confirmPassword.isNotBlank()) {
                        if (password == confirmPassword) {
                            authViewModel.register(name, email, password, confirmPassword)
                        }
                    }
                },
                enabled = !authState.isLoading && name.isNotBlank() && 
                         email.isNotBlank() && password.isNotBlank() && 
                         confirmPassword.isNotBlank() && password == confirmPassword,
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
                        text = "Sign Up",
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
                    Text(
                        text = "Already have an account?",
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onBackground,
                        fontSize = 14.sp,
                    )

                    Spacer(modifier = Modifier.width(4.dp))


                    Text(
                        modifier = Modifier
                        .clickable { onLoginClick() },
                        text = "Sign In",
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.primary,
                        fontSize = 14.sp
                    )
                }
            }

        }
    }
}
