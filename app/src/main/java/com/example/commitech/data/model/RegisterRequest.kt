package com.example.commitech.data.model

/**
 * Register Request - Include device info untuk multi-device tracking
 * 
 * INSTAGRAM-STYLE:
 * - Send device info saat register
 * - Auto-login after register dengan device info
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
    val device_name: String,      // "Samsung Galaxy S21"
    val device_type: String,      // "android"
    val device_id: String         // Unique device identifier
)
