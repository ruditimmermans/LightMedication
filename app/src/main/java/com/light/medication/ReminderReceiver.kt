package com.light.medication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.light.medication.data.AppDatabase
import com.light.medication.data.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAll(context)
            return
        }

        val id = intent.getIntExtra("REMINDER_ID", -1)
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: "Medication"
        val pillCount = intent.getStringExtra("PILL_COUNT") ?: "1"
        val hour = intent.getIntExtra("HOUR", 8)
        val minute = intent.getIntExtra("MINUTE", 0)

        showNotification(context, medicationName, pillCount, id)
        
        // Reschedule for tomorrow
        val scheduler = ReminderScheduler(context)
        scheduler.scheduleDailyReminder(
            Reminder(id = id, medicationName = medicationName, pillCount = pillCount, hour = hour, minute = minute)
        )
    }

    private fun rescheduleAll(context: Context) {
        val db = AppDatabase.getDatabase(context)
        val scheduler = ReminderScheduler(context)
        CoroutineScope(Dispatchers.IO).launch {
            db.reminderDao().getAllReminders().collect { reminders ->
                reminders.filter { it.isEnabled }.forEach { reminder ->
                    scheduler.scheduleDailyReminder(reminder)
                }
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

        val activityIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_content, pillCount, medicationName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
