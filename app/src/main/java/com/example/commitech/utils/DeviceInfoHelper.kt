package com.example.commitech.utils

import android.content.Context
import android.os.Build
import java.util.UUID
import androidx.core.content.edit

object DeviceInfoHelper {

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

    fun getDeviceType(): String {
        return "android"
    }

    fun getDeviceId(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(
            "device_info",
            Context.MODE_PRIVATE
        )
        
        var deviceId = sharedPrefs.getString("device_id", null)
        
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()

            sharedPrefs.edit {
                putString("device_id", deviceId)
            }
        }
        
        return deviceId
    }


}
