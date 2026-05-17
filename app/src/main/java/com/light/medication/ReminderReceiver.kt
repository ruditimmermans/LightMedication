package com.light.medication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.light.medication.data.AppDatabase
import com.light.medication.data.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAll(context)
            return
        }

        val id = intent.getIntExtra("REMINDER_ID", -1)
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: context.getString(R.string.default_medication_name)
        val pillCount = intent.getStringExtra("PILL_COUNT") ?: "1"
        val hour = intent.getIntExtra("HOUR", 8)
        val minute = intent.getIntExtra("MINUTE", 0)
        val frequency = intent.getStringExtra("FREQUENCY") ?: "Daily"

        showNotification(context, medicationName, pillCount, id)
        
        // Reschedule based on frequency
        val scheduler = ReminderScheduler(context)
        scheduler.scheduleReminder(
            Reminder(id = id, medicationName = medicationName, pillCount = pillCount, hour = hour, minute = minute, frequency = frequency)
        )
    }

    private fun rescheduleAll(context: Context) {
        val db = AppDatabase.getDatabase(context)
        val scheduler = ReminderScheduler(context)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = db.reminderDao().getAllReminders().first()
                reminders.filter { it.isEnabled }.forEach { reminder ->
                    scheduler.scheduleReminder(reminder)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, medicationName: String, pillCount: String, id: Int) {
        val channelId = "medication_reminder_channel"
        val notificationId = if (id != -1) id else 1

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(context, ActionReceiver::class.java).apply {
            putExtra("REMINDER_ID", notificationId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissedIntent = Intent(context, ActionReceiver::class.java).apply {
            action = "ACTION_DISMISSED"
            putExtra("REMINDER_ID", notificationId)
        }
        val dismissedPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1000, // Use a different request code
            dismissedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_content, pillCount, medicationName))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.notification_content, pillCount, medicationName)))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(dismissedPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
