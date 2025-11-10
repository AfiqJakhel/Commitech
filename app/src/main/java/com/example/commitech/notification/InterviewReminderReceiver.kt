package com.example.commitech.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class InterviewReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val participantName = intent.getStringExtra(EXTRA_PARTICIPANT_NAME) ?: return
        val scheduleLabel = intent.getStringExtra(EXTRA_SCHEDULE_LABEL) ?: ""

        InterviewNotificationHelper.ensureChannels(context)
        InterviewNotificationHelper.showReminderNotification(
            context = context,
            participantName = participantName,
            scheduleLabel = scheduleLabel
        )
    }

    companion object {
        const val EXTRA_PARTICIPANT_NAME = "extra_participant_name"
        const val EXTRA_SCHEDULE_LABEL = "extra_schedule_label"
    }
}

