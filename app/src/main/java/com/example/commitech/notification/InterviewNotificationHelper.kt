package com.example.commitech.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.commitech.MainActivity
import com.example.commitech.R

/**
 * InterviewNotificationHelper
 *
 * Helper object untuk mengelola notifikasi lokal terkait wawancara.
 *
 * FITUR:
 * - Reminder notification: 10 menit sebelum wawancara dimulai
 * - Warning notification: 5 menit sebelum wawancara selesai
 * - Completion notification: Saat wawancara selesai
 *
 * NOTIFICATION CHANNELS:
 * - REMINDER_CHANNEL_ID: Channel untuk reminder (priority: DEFAULT)
 * - WARNING_CHANNEL_ID: Channel untuk warning (priority: HIGH, dengan vibration)
 *
 * CARA PENGGUNAAN:
 * ```kotlin
 * // 1. Pastikan channels sudah dibuat (biasanya di MainActivity.onCreate)
 * InterviewNotificationHelper.ensureChannels(context)
 *
 * // 2. Tampilkan notifikasi
 * InterviewNotificationHelper.showReminderNotification(
 *     context = context,
 *     participantName = "Budi Santoso",
 *     scheduleLabel = "10:00 WIB"
 * )
 * ```
 *
 * CATATAN PENTING:
 * - Icon notifikasi HARUS monochrome (putih) untuk Android notification tray
 * - PendingIntent membuat notifikasi bisa dibuka dan masuk ke aplikasi
 * - autoCancel(true) membuat notifikasi hilang saat di-tap
 *
 * @author Commitech Team
 * @since 2025-12-05
 */
object InterviewNotificationHelper {

    // Notification Channel IDs
    const val REMINDER_CHANNEL_ID = "interview_reminder_channel"
    const val WARNING_CHANNEL_ID = "interview_warning_channel"
    const val JADWAL_REMINDER_CHANNEL_ID = "jadwal_reminder_channel"

    // Intent extras keys untuk deep linking
    const val EXTRA_PARTICIPANT_NAME = "extra_participant_name"
    const val EXTRA_SCHEDULE_LABEL = "extra_schedule_label"
    const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"

    // Notification types
    const val TYPE_REMINDER = "reminder"
    const val TYPE_WARNING = "warning"
    const val TYPE_COMPLETION = "completion"

    /**
     * Membuat notification channels untuk Android O (API 26) ke atas.
     *
     * Channels harus dibuat sebelum menampilkan notifikasi.
     * Biasanya dipanggil di MainActivity.onCreate() atau saat app pertama kali dibuka.
     *
     * CHANNELS:
     * 1. REMINDER_CHANNEL: Untuk reminder wawancara (priority: DEFAULT)
     * 2. WARNING_CHANNEL: Untuk warning waktu habis (priority: HIGH + vibration)
     *
     * @param context Application context
     */
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

    /**
     * Menampilkan notifikasi reminder wawancara.
     *
     * Dipanggil 10 menit sebelum wawancara dimulai oleh InterviewReminderReceiver.
     *
     * FITUR:
     * - Icon: Bell notification (monochrome white)
     * - Priority: DEFAULT
     * - Tap notification: Buka aplikasi ke halaman utama
     * - Auto cancel: Notifikasi hilang saat di-tap
     *
     * @param context Application context
     * @param participantName Nama peserta wawancara (contoh: "Budi Santoso")
     * @param scheduleLabel Label jadwal (contoh: "10:00 WIB")
     */
    fun showReminderNotification(
        context: Context,
        participantName: String,
        scheduleLabel: String
    ) {
        // Check permission untuk Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        
        // Buat intent untuk membuka aplikasi saat notifikasi di-tap
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PARTICIPANT_NAME, participantName)
            putExtra(EXTRA_SCHEDULE_LABEL, scheduleLabel)
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_REMINDER)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            ("reminder-$participantName-$scheduleLabel").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Logo Commitech berwarna penuh (Large Icon di kanan)
        val largeIcon = android.graphics.BitmapFactory.decodeResource(
            context.resources,
            R.drawable.commitechlogo
        )

        // STYLE: Unified Commitech Style (Sama untuk semua tipe)
        // Color: Purple 500 (Brand Color)
        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Ganti logo CT dengan icon Lonceng biasa (Wajib ada)
            .setLargeIcon(largeIcon)
            .setContentTitle("Wawancara: $participantName")
            .setContentText("⏰ $scheduleLabel · Segera bersiap")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("⏰ $scheduleLabel · Segera bersiap\nKetuk untuk melihat detail jadwal")
            )
            // .setColor dihapus agar icon kiri jadi warna default sistem (biasanya abu-abu/hitam netral)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setShowWhen(true)

        NotificationManagerCompat.from(context).notify(
            ("reminder-$participantName-$scheduleLabel").hashCode(),
            builder.build()
        )
    }

    /**
     * Menampilkan notifikasi warning waktu wawancara hampir habis.
     *
     * Dipanggil 5 menit sebelum wawancara selesai.
     *
     * FITUR:
     * - Icon: Triangle warning (monochrome white)
     * - Priority: HIGH (muncul di atas notifikasi lain)
     * - Vibration: Enabled (dari channel settings)
     * - Tap notification: Buka aplikasi
     * - Auto cancel: Notifikasi hilang saat di-tap
     *
     * @param context Application context
     * @param participantName Nama peserta wawancara
     * @param scheduleLabel Label jadwal
     */
    fun showWarningNotification(
        context: Context,
        participantName: String,
        scheduleLabel: String
    ) {
        // Check permission untuk Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        
        // Buat intent untuk membuka aplikasi
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PARTICIPANT_NAME, participantName)
            putExtra(EXTRA_SCHEDULE_LABEL, scheduleLabel)
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_WARNING)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            ("warning-$participantName-$scheduleLabel").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Logo Commitech berwarna penuh (Large Icon di kanan)
        val largeIcon = android.graphics.BitmapFactory.decodeResource(
            context.resources,
            R.drawable.commitechlogo
        )

        // STYLE: Unified Commitech Style (Desain SAMA, isi BEDA)
        val builder = NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Ganti logo CT dengan icon Lonceng biasa
            .setLargeIcon(largeIcon)
            .setContentTitle("Sisa Waktu: 5 Menit")
            .setContentText("⚠️ $participantName · Segera selesaikan")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("⚠️ $participantName · Segera selesaikan\nKetuk untuk membuka sesi wawancara")
            )
            // .setColor dihapus agar icon kiri netral
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT) // Samakan category
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setShowWhen(true)

        NotificationManagerCompat.from(context).notify(
            ("warning-$participantName-$scheduleLabel").hashCode(),
            builder.build()
        )
    }

    /**
     * Menampilkan notifikasi wawancara selesai.
     *
     * Dipanggil saat wawancara telah selesai dilakukan.
     *
     * FITUR:
     * - Icon: Checkmark in circle (monochrome white)
     * - Priority: DEFAULT
     * - Tap notification: Buka aplikasi
     * - Auto cancel: Notifikasi hilang saat di-tap
     *
     * @param context Application context
     * @param participantName Nama peserta wawancara
     * @param scheduleLabel Label jadwal
     */
    fun showCompletionNotification(
        context: Context,
        participantName: String,
        scheduleLabel: String
    ) {
        // Check permission untuk Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        
        // Buat intent untuk membuka aplikasi
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PARTICIPANT_NAME, participantName)
            putExtra(EXTRA_SCHEDULE_LABEL, scheduleLabel)
            putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_COMPLETION)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            ("done-$participantName-$scheduleLabel").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Logo Commitech berwarna penuh (Large Icon di kanan)
        val largeIcon = android.graphics.BitmapFactory.decodeResource(
            context.resources,
            R.drawable.commitechlogo
        )

        // STYLE: Unified Commitech Style (Desain SAMA, isi BEDA)
        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Ganti logo CT dengan icon Lonceng biasa
            .setLargeIcon(largeIcon)
            .setContentTitle("Wawancara Selesai")
            .setContentText("✅ $participantName · Berjalan lancar")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("✅ $participantName · Berjalan lancar\nKetuk untuk melihat hasil wawancara")
            )
            // .setColor dihapus agar icon kiri netral
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Samakan priority
            .setCategory(NotificationCompat.CATEGORY_EVENT) // Samakan category
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setShowWhen(true)

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

