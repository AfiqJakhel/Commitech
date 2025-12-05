package com.example.commitech.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class InterviewReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val participantName = intent.getStringExtra(EXTRA_PARTICIPANT_NAME) ?: return
            val scheduleLabel = intent.getStringExtra(EXTRA_SCHEDULE_LABEL) ?: ""
            val pewawancara = intent.getStringExtra(EXTRA_PEWAWANCARA) ?: ""

            // Pastikan channel notification sudah dibuat
            InterviewNotificationHelper.ensureChannels(context)
            
            // Jika ada pewawancara, gunakan notifikasi khusus jadwal rekrutmen
            if (pewawancara.isNotEmpty() && pewawancara != "-") {
                InterviewNotificationHelper.showJadwalReminderNotification(
                    context = context,
                    judulJadwal = participantName,
                    scheduleLabel = scheduleLabel,
                    pewawancara = pewawancara
                )
                Log.d("InterviewReminder", "Notifikasi jadwal dikirim: $participantName - Pewawancara: $pewawancara")
            } else {
                InterviewNotificationHelper.showReminderNotification(
                    context = context,
                    participantName = participantName,
                    scheduleLabel = scheduleLabel
                )
            }
        } catch (e: Exception) {
            Log.e("InterviewReminder", "Error saat menampilkan notifikasi", e)
        }
    }

    companion object {
        const val EXTRA_PARTICIPANT_NAME = "extra_participant_name"
        const val EXTRA_SCHEDULE_LABEL = "extra_schedule_label"
        const val EXTRA_PEWAWANCARA = "extra_pewawancara"
    }
}

