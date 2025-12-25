package com.example.commitech.data.model

data class SessionValidationResponse(
    val isValid: Boolean,
    val user: User? = null,
    val daysRemaining: Int? = null,
    val expiresAt: String? = null,
    val message: String? = null
)

data class ActiveSessionsResponse(
    val sessions: List<SessionInfo>,
    val totalSessions: Int
)

data class SessionInfo(
    val id: String,
    val deviceName: String,
    val deviceType: String,
    val ipAddress: String?,
    val location: String,
    val lastActivity: String,
    val lastActivityTimestamp: Long,
    val createdAt: String,
    val expiresAt: String,
    val daysRemaining: Int,
    val isCurrent: Boolean
)
