package com.example.commitech.data.model

/**
 * Session Model - Representasi session dari server
 * 
 * INSTAGRAM-STYLE SESSION:
 * - Session persist di database
 * - Multi-device support
 * - Track device info, location, last activity
 */
data class Session(
    val id: String,              // Session token
    val userId: Int,
    val deviceName: String?,     // "Samsung Galaxy S21"
    val deviceType: String?,     // "android", "ios", "web"
    val deviceId: String?,       // Unique device identifier
    val ipAddress: String?,
    val userAgent: String?,
    val location: String?,       // "Jakarta, Indonesia"
    val createdAt: Long,         // Login timestamp
    val lastActivity: Long,      // Last active timestamp
    val expiresAt: Long          // Expiry timestamp (7 days)
)

/**
 * Session Validation Response
 * 
 * Response dari API /api/session/check
 * 
 * INSTAGRAM-STYLE:
 * - Check expiry based on created_at (7 days)
 * - Return user data if valid
 * - Return days remaining
 */
data class SessionValidationResponse(
    val isValid: Boolean,
    val user: User? = null,
    val daysRemaining: Int? = null,  // Days until expiry
    val expiresAt: String? = null,   // Expiry date string
    val message: String? = null
)

/**
 * Active Sessions Response
 * 
 * Response dari API /api/session/list
 * 
 * INSTAGRAM-STYLE:
 * - List all active sessions untuk user
 * - Show device info, location, last active
 * - Support logout dari device tertentu
 */
data class ActiveSessionsResponse(
    val sessions: List<SessionInfo>,
    val totalSessions: Int
)

/**
 * Session Info - Detail session untuk display di UI
 * 
 * USE CASE: Active Sessions screen
 */
data class SessionInfo(
    val id: String,                    // Session token
    val deviceName: String,            // "Samsung Galaxy S21"
    val deviceType: String,            // "android", "ios", "web"
    val ipAddress: String?,            // "192.168.1.100"
    val location: String,              // "Jakarta, Indonesia"
    val lastActivity: String,          // "2 hours ago"
    val lastActivityTimestamp: Long,   // Timestamp untuk sorting
    val createdAt: String,             // "05 Dec 2025 10:00"
    val expiresAt: String,             // "12 Dec 2025 10:00"
    val daysRemaining: Int,            // 6 days
    val isCurrent: Boolean             // Is this the current session?
)
