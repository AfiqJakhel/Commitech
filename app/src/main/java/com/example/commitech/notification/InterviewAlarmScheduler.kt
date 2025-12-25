package com.example.commitech.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import com.example.commitech.ui.viewmodel.ReminderSchedule
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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

  
    fun scheduleJadwalReminder(
        context: Context,
        jadwalId: Int,
        judul: String,
        tanggalMulai: String,
        waktuMulai: String,
        pewawancara: String
    ) {
        try {
            val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID"))
            val timeFormatter = DateTimeFormatter.ofPattern("HH.mm", Locale("id", "ID"))
            
            val tanggalJadwal = LocalDate.parse(tanggalMulai, dateFormatter)
            val waktuJadwal = LocalTime.parse(waktuMulai.replace(".", ":"), timeFormatter)
            
            // H-1: sehari sebelum jadwal, jam 08:00 pagi
            val tanggalReminder = tanggalJadwal.minusDays(1)
            val waktuReminder = LocalTime.of(8, 0) // Jam 8 pagi
            
            val reminderDateTime = tanggalReminder.atTime(waktuReminder)
            val triggerMillis = reminderDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            // Jika waktu reminder sudah lewat, skip
            if (triggerMillis <= System.currentTimeMillis()) {
                return
            }
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            
            val intent = Intent(context, InterviewReminderReceiver::class.java).apply {
                putExtra(InterviewReminderReceiver.EXTRA_PARTICIPANT_NAME, judul)
                putExtra(InterviewReminderReceiver.EXTRA_SCHEDULE_LABEL, "$tanggalMulai pukul $waktuMulai")
                putExtra(InterviewReminderReceiver.EXTRA_PEWAWANCARA, pewawancara)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ("jadwal-$jadwalId").hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            // Jika parsing gagal, skip scheduling
            e.printStackTrace()
        }
    }
}

