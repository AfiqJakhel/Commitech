package com.example.commitech.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class InterviewReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val participantName = intent.getStringExtra(EXTRA_PARTICIPANT_NAME) ?: return
        val scheduleLabel = intent.getStringExtra(EXTRA_SCHEDULE_LABEL) ?: ""
        val pewawancara = intent.getStringExtra(EXTRA_PEWAWANCARA)

        InterviewNotificationHelper.ensureChannels(context)
        
        if (!pewawancara.isNullOrEmpty()) {
            // Ini adalah reminder jadwal rekrutmen
            InterviewNotificationHelper.showJadwalReminderNotification(
                context = context,
                judulJadwal = participantName, // participantName digunakan sebagai judul jadwal
                scheduleLabel = scheduleLabel,
                pewawancara = pewawancara
            )
        } else {
            // Ini adalah reminder wawancara biasa (per peserta)
            InterviewNotificationHelper.showReminderNotification(
                context = context,
                participantName = participantName,
                scheduleLabel = scheduleLabel
            )
        }
    }

    companion object {
        const val EXTRA_PARTICIPANT_NAME = "extra_participant_name"
        const val EXTRA_SCHEDULE_LABEL = "extra_schedule_label"
        const val EXTRA_PEWAWANCARA = "extra_pewawancara"
    }
}

