package com.example.commitech.data.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
    val device_name: String,
    val device_type: String,
    val device_id: String
)
