package com.example.commitech.notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission

class InterviewReminderReceiver : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val participantName = intent.getStringExtra(EXTRA_PARTICIPANT_NAME) ?: return
        val scheduleLabel = intent.getStringExtra(EXTRA_SCHEDULE_LABEL) ?: ""
        val pewawancara = intent.getStringExtra(EXTRA_PEWAWANCARA)

        InterviewNotificationHelper.ensureChannels(context)
        
        if (!pewawancara.isNullOrEmpty()) {
            InterviewNotificationHelper.showJadwalReminderNotification(
                context = context,
                judulJadwal = participantName,
                scheduleLabel = scheduleLabel,
                pewawancara = pewawancara
            )
        } else {

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

