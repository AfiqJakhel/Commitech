package com.example.commitech.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * InterviewReminderReceiver
 *
 * BroadcastReceiver yang menerima alarm dari AlarmManager untuk menampilkan
 * notifikasi reminder wawancara.
 *
 * CARA KERJA:
 * 1. InterviewAlarmScheduler set alarm dengan AlarmManager
 * 2. Saat waktu tiba, AlarmManager trigger receiver ini
 * 3. Receiver mengambil data dari Intent (nama peserta, jadwal)
 * 4. Receiver memanggil InterviewNotificationHelper untuk show notification
 *
 * INTENT EXTRAS:
 * - EXTRA_PARTICIPANT_NAME: Nama peserta wawancara (String)
 * - EXTRA_SCHEDULE_LABEL: Label jadwal (String, contoh: "10:00 WIB")
 *
 * CONTOH PENGGUNAAN:
 * ```kotlin
 * val intent = Intent(context, InterviewReminderReceiver::class.java).apply {
 *     putExtra(EXTRA_PARTICIPANT_NAME, "Budi Santoso")
 *     putExtra(EXTRA_SCHEDULE_LABEL, "10:00 WIB")
 * }
 * val pendingIntent = PendingIntent.getBroadcast(
 *     context,
 *     requestCode,
 *     intent,
 *     PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
 * )
 * alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
 * ```
 *
 * REGISTERED IN:
 * - AndroidManifest.xml sebagai <receiver> dengan android:exported="false"
 *
 * @author Commitech Team
 * @since 2025-12-05
 */
class InterviewReminderReceiver : BroadcastReceiver() {

    /**
     * Dipanggil saat alarm trigger.
     *
     * Method ini akan:
     * 1. Extract data dari Intent (nama peserta & jadwal)
     * 2. Ensure notification channels sudah dibuat
     * 3. Tampilkan reminder notification
     *
     * @param context Application context
     * @param intent Intent yang berisi data peserta dan jadwal
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Extract data dari intent
        val participantName = intent.getStringExtra(EXTRA_PARTICIPANT_NAME) ?: return
        val scheduleLabel = intent.getStringExtra(EXTRA_SCHEDULE_LABEL) ?: ""

        // Pastikan notification channels sudah dibuat
        InterviewNotificationHelper.ensureChannels(context)
        
        // Tampilkan reminder notification
        InterviewNotificationHelper.showReminderNotification(
            context = context,
            participantName = participantName,
            scheduleLabel = scheduleLabel
        )
    }

    companion object {
        /**
         * Key untuk nama peserta di Intent extras.
         * Value: String (contoh: "Budi Santoso")
         */
        const val EXTRA_PARTICIPANT_NAME = "extra_participant_name"
        
        /**
         * Key untuk label jadwal di Intent extras.
         * Value: String (contoh: "10:00 WIB")
         */
        const val EXTRA_SCHEDULE_LABEL = "extra_schedule_label"
    }
}

