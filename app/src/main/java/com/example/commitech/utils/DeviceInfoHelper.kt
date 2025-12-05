package com.example.commitech.utils

import android.content.Context
import android.os.Build
import java.util.UUID

/**
 * DeviceInfoHelper - Collect device information untuk multi-device tracking
 * 
 * INSTAGRAM-STYLE SESSION:
 * - Track device name (Samsung Galaxy S21)
 * - Track device type (android)
 * - Generate unique device ID
 * - Show di "Active Sessions" screen
 * 
 * USE CASE:
 * - Multi-device login tracking
 * - Security: Detect suspicious login
 * - User bisa logout dari device tertentu
 */
object DeviceInfoHelper {
    
    /**
     * Get device name untuk display
     * 
     * Examples:
     * - "Samsung Galaxy S21"
     * - "Xiaomi Redmi Note 10"
     * - "Google Pixel 6"
     * 
     * @return Device name yang user-friendly
     */
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
        val model = Build.MODEL
        
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }
    
    /**
     * Get device type
     * 
     * @return "android" (untuk Android devices)
     */
    fun getDeviceType(): String {
        return "android"
    }
    
    /**
     * Get unique device ID
     * 
     * CRITICAL: Ini untuk identify device yang sama
     * - Jika user logout lalu login lagi di device yang sama, device_id tetap sama
     * - Jika user uninstall app, device_id akan berubah (new install)
     * - Device ID persist di SharedPreferences
     * 
     * USE CASE:
     * - Prevent duplicate sessions untuk device yang sama
     * - Update existing session instead of create new
     * 
     * @param context Application context
     * @return Unique device ID (UUID format)
     */
    fun getDeviceId(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(
            "device_info",
            Context.MODE_PRIVATE
        )
        
        var deviceId = sharedPrefs.getString("device_id", null)
        
        if (deviceId == null) {
            // Generate new device ID (first time)
            deviceId = UUID.randomUUID().toString()
            
            // Save untuk next time
            sharedPrefs.edit()
                .putString("device_id", deviceId)
                .apply()
        }
        
        return deviceId
    }
    
    /**
     * Get Android OS version
     * 
     * Examples:
     * - "Android 13"
     * - "Android 11"
     * - "Android 14"
     * 
     * @return OS version string
     */
    fun getOsVersion(): String {
        return "Android ${Build.VERSION.RELEASE}"
    }
    
    /**
     * Get full device info untuk display di UI
     * 
     * Format: "Device Name • OS Version"
     * 
     * Examples:
     * - "Samsung Galaxy S21 • Android 13"
     * - "Xiaomi Redmi Note 10 • Android 11"
     * 
     * @return Formatted device info string
     */
    fun getDeviceDisplayName(): String {
        return "${getDeviceName()} • ${getOsVersion()}"
    }
    
    /**
     * Get device manufacturer
     * 
     * Examples:
     * - "Samsung"
     * - "Xiaomi"
     * - "Google"
     * 
     * @return Manufacturer name
     */
    fun getManufacturer(): String {
        return Build.MANUFACTURER.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }
    
    /**
     * Get device model
     * 
     * Examples:
     * - "Galaxy S21"
     * - "Redmi Note 10"
     * - "Pixel 6"
     * 
     * @return Model name
     */
    fun getModel(): String {
        return Build.MODEL
    }
}
