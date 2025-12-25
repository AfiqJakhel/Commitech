package com.example.commitech.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.commitech.R

object InterviewNotificationHelper {

    const val REMINDER_CHANNEL_ID = "interview_reminder_channel"
    const val WARNING_CHANNEL_ID = "interview_warning_channel"

    const val EXTRA_PARTICIPANT_NAME = "extra_participant_name"
    const val EXTRA_SCHEDULE_LABEL = "extra_schedule_label"
    const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"

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

        notificationManager.createNotificationChannel(reminderChannel)
        notificationManager.createNotificationChannel(warningChannel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showJadwalReminderNotification(
        context: Context,
        judulJadwal: String,
        scheduleLabel: String,
        pewawancara: String
    ) {
        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Pengingat Jadwal: $judulJadwal")
            .setContentText("$scheduleLabel - Pewawancara: $pewawancara")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(
            ("jadwal-reminder-$judulJadwal-$scheduleLabel").hashCode(),
            builder.build()
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showJadwalUrgentNotification(
        context: Context,
        judulJadwal: String,
        tanggalMulai: String,
        waktuMulai: String,
        waktuSelesai: String,
        pewawancara: String,
        isToday: Boolean
    ) {
        val urgentText = if (isToday) "HARI INI" else "BESOK"
        val builder = NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_warning)
            .setContentTitle("⚠️ JADWAL $urgentText: $judulJadwal")
            .setContentText("$tanggalMulai, $waktuMulai - $waktuSelesai | Pewawancara: $pewawancara")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$tanggalMulai, $waktuMulai - $waktuSelesai\nPewawancara: $pewawancara"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(
            ("jadwal-urgent-$judulJadwal-$tanggalMulai").hashCode(),
            builder.build()
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
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

}

