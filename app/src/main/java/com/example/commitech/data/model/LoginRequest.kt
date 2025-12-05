package com.example.commitech.data.model

/**
 * Login Request - Include device info untuk multi-device tracking
 * 
 * INSTAGRAM-STYLE:
 * - Send device info saat login
 * - Server store device info di sessions table
 * - Support multi-device tracking
 */
data class LoginRequest(
    val email: String,
    val password: String,
    val device_name: String,
    val device_type: String,
    val device_id: String
    val device_name: String,      // "Samsung Galaxy S21"
    val device_type: String,      // "android"
    val device_id: String         // Unique device identifier
)
