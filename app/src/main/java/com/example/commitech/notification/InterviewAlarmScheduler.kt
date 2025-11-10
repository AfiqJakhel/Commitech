package com.example.commitech.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import com.example.commitech.ui.viewmodel.ReminderSchedule

object InterviewAlarmScheduler {

    fun scheduleReminder(context: Context, schedule: ReminderSchedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, InterviewReminderReceiver::class.java).apply {
            putExtra(InterviewReminderReceiver.EXTRA_PARTICIPANT_NAME, schedule.participantName)
            putExtra(InterviewReminderReceiver.EXTRA_SCHEDULE_LABEL, schedule.scheduleLabel)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.key.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            schedule.triggerAtMillis,
            pendingIntent
        )
    }
}

