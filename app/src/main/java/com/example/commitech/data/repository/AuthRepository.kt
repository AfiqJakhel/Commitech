package com.example.commitech.data.repository

import com.example.commitech.data.api.RetrofitClient
import com.example.commitech.data.model.AuthResponse
import com.example.commitech.data.model.LoginRequest
import com.example.commitech.data.model.RegisterRequest
import com.example.commitech.data.model.SessionValidationResponse
import com.example.commitech.data.model.ActiveSessionsResponse
import retrofit2.Response

/**
 * AuthRepository - Handle authentication & session management
 * 
 * HYBRID SESSION APPROACH:
 * - Login/Register: Server creates session in database
 * - Token: Stored in memory only (ViewModel)
 * - Validation: Server-side via API calls
 * - Cache: Client-side untuk reduce network calls
 */
class AuthRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        deviceName: String,
        deviceType: String,
        deviceId: String
    ): Response<AuthResponse> {
        val request = RegisterRequest(
            name, 
            email, 
            password, 
            passwordConfirmation,
            deviceName,
            deviceType,
            deviceId
        )
        return apiService.register(request)
    }
    
    suspend fun login(
        email: String,
        password: String,
        deviceName: String,
        deviceType: String,
        deviceId: String
    ): Response<AuthResponse> {
        val request = LoginRequest(
            email = email,
            password = password,
            device_name = deviceName,
            device_type = deviceType,
            device_id = deviceId
        )
        return apiService.login(request)
    }
    
    suspend fun logout(token: String): Response<Unit> {
        return apiService.logout("Bearer $token")
    }
    
    suspend fun getUser(token: String): Response<AuthResponse> {
        return apiService.getUser("Bearer $token")
    }
    
    // ============================================================================
    // HYBRID SESSION MANAGEMENT - New Functions
    // ============================================================================
    
    /**
     * Check session validity dengan server
     * 
     * CRITICAL: Ini adalah core dari hybrid session approach
     * - Dipanggil saat app resume
     * - Dipanggil saat cache expired (5 menit)
     * - Server validate token & check expiry
     * 
     * @param token Session token
     * @return SessionValidationResponse dengan isValid flag
     */
    suspend fun checkSession(token: String): Response<SessionValidationResponse> {
        return apiService.checkSession("Bearer $token")
    }
    
    /**
     * Get list of active sessions untuk current user
     * 
     * USE CASE: Settings → Active Sessions
     * User bisa lihat device mana saja yang sedang login
     * 
     * @param token Session token
     * @return List of active sessions
     */
    suspend fun getActiveSessions(token: String): Response<ActiveSessionsResponse> {
        return apiService.getActiveSessions("Bearer $token")
    }
    
    /**
     * Revoke specific session by ID
     * 
     * USE CASE: User logout dari device tertentu
     * 
     * @param token Current session token
     * @param sessionId Session ID to revoke (String, bukan Int)
     */
    suspend fun revokeSession(token: String, sessionId: String): Response<Unit> {
        return apiService.revokeSession("Bearer $token", sessionId)
    }
    
    /**
     * Revoke all other sessions except current
     * 
     * USE CASE: "Logout from all other devices"
     * 
     * @param token Current session token
     */
    suspend fun revokeOtherSessions(token: String): Response<Unit> {
        return apiService.revokeOtherSessions("Bearer $token")
    }
}
