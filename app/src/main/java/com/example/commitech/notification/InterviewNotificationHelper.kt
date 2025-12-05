package com.example.commitech.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.commitech.R

object InterviewNotificationHelper {

    const val REMINDER_CHANNEL_ID = "interview_reminder_channel"
    const val WARNING_CHANNEL_ID = "interview_warning_channel"
    const val JADWAL_REMINDER_CHANNEL_ID = "jadwal_reminder_channel"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val reminderChannel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            context.getString(R.string.channel_interview_reminder),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_interview_reminder_desc)
        }

        val warningChannel = NotificationChannel(
            WARNING_CHANNEL_ID,
            context.getString(R.string.channel_interview_warning),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_interview_warning_desc)
            enableVibration(true)
        }

        // Channel khusus untuk jadwal rekrutmen dengan importance tinggi
        val jadwalReminderChannel = NotificationChannel(
            JADWAL_REMINDER_CHANNEL_ID,
            "Pengingat Jadwal Wawancara",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi pengingat jadwal wawancara sehari sebelum jadwal"
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
        }

        notificationManager.createNotificationChannel(reminderChannel)
        notificationManager.createNotificationChannel(warningChannel)
        notificationManager.createNotificationChannel(jadwalReminderChannel)
    }

    fun showReminderNotification(
        context: Context,
        participantName: String,
        scheduleLabel: String
    ) {
        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_reminder_title))
            .setContentText(
                context.getString(
                    R.string.notification_reminder_body,
                    participantName,
                    scheduleLabel
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(
            ("reminder-$participantName-$scheduleLabel").hashCode(),
            builder.build()
        )
    }

    fun showWarningNotification(
        context: Context,
        participantName: String,
        scheduleLabel: String
    ) {
        val builder = NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_warning)
            .setContentTitle(context.getString(R.string.notification_warning_title))
            .setContentText(
                context.getString(
                    R.string.notification_warning_body,
                    participantName
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(
            ("warning-$participantName-$scheduleLabel").hashCode(),
            builder.build()
        )
    }

    fun showCompletionNotification(
        context: Context,
        participantName: String,
        scheduleLabel: String
    ) {
        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_done)
            .setContentTitle(context.getString(R.string.notification_complete_title, participantName))
            .setContentText(
                context.getString(
                    R.string.notification_complete_body,
                    scheduleLabel
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(
            ("done-$participantName-$scheduleLabel").hashCode(),
            builder.build()
        )
    }

    fun showJadwalReminderNotification(
        context: Context,
        judulJadwal: String,
        scheduleLabel: String,
        pewawancara: String
    ) {
        // Pastikan channel sudah dibuat
        ensureChannels(context)
        
        val notificationId = ("jadwal-reminder-$judulJadwal-$scheduleLabel").hashCode()
        
        val builder = NotificationCompat.Builder(context, JADWAL_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🔔 Pengingat Jadwal Wawancara")
            .setContentText("$judulJadwal akan berlangsung besok pada $scheduleLabel. Pewawancara: $pewawancara")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$judulJadwal akan berlangsung besok pada $scheduleLabel.\n\nPewawancara: $pewawancara\n\nJangan lupa untuk mempersiapkan diri!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, lights
            .setAutoCancel(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Tampilkan di lock screen

        // Cek apakah notifikasi diizinkan
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }

    /**
     * Tampilkan notifikasi langsung untuk jadwal hari ini atau besok
     */
    fun showJadwalUrgentNotification(
        context: Context,
        judulJadwal: String,
        tanggalMulai: String,
        waktuMulai: String,
        waktuSelesai: String,
        pewawancara: String,
        isToday: Boolean
    ) {
        // Pastikan channel sudah dibuat
        ensureChannels(context)
        
        val notificationId = ("jadwal-urgent-$judulJadwal-$tanggalMulai").hashCode()
        
        val waktuText = if (waktuSelesai.isNotBlank() && waktuSelesai != "-") {
            "$waktuMulai - $waktuSelesai"
        } else {
            waktuMulai
        }
        
        val title = if (isToday) {
            "⚠️ JADWAL WAWANCARA HARI INI!"
        } else {
            "🔔 JADWAL WAWANCARA BESOK!"
        }
        
        val message = if (isToday) {
            "$judulJadwal berlangsung HARI INI pada pukul $waktuText. Pewawancara: $pewawancara"
        } else {
            "$judulJadwal akan berlangsung BESOK pada $tanggalMulai pukul $waktuText. Pewawancara: $pewawancara"
        }
        
        val builder = NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_warning)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$message\n\n⚠️ Segera persiapkan diri untuk wawancara!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, lights
            .setAutoCancel(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Tampilkan di lock screen
            .setOngoing(false)

        // Cek apakah notifikasi diizinkan
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }
}

