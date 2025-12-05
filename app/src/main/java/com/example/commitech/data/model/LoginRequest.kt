package com.example.commitech.data.model

data class LoginRequest(
    val email: String,
    val password: String,
    val device_name: String,
    val device_type: String,
    val device_id: String
)
