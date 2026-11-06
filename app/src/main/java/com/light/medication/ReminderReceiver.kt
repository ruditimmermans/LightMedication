package com.light.medication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.light.medication.data.AppDatabase
import com.light.medication.data.Reminder
import com.light.medication.util.AutoClearPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            rescheduleAll(context)
            if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
                if (com.light.medication.util.BigmePreferences.isBigmeModeEnabled(context)) {
                    com.light.medication.util.BigmePreferences.startKeepAliveService(context)
                    try {
                        val activityIntent = Intent(context, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                        }
                        context.startActivity(activityIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return
        }

        val id = intent.getIntExtra("REMINDER_ID", -1)
        if (id == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val reminders = db.reminderDao().getAllReminders().first()
                val reminder = reminders.find { it.id == id }
                
                if (reminder != null && reminder.isEnabled) {
                    if (shouldShowNotification(reminder)) {
                        showNotification(context, reminder.medicationName, reminder.pillCount, reminder.id)
                    }
                    
                    AutoClearPreferences.performAutoClear(context)

                    // Always reschedule for the next occurrence
                    val scheduler = ReminderScheduler(context)
                    scheduler.scheduleReminder(reminder)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun shouldShowNotification(reminder: Reminder): Boolean {
        val lastAction = maxOf(reminder.lastTakenTimestamp ?: 0L, reminder.lastSkippedTimestamp ?: 0L)
        val now = LocalDateTime.now(ZoneId.systemDefault())
        
        val scheduledToday = now.withHour(reminder.hour)
            .withMinute(reminder.minute)
            .withSecond(0)
            .withNano(0)

        // Don't show notification if it is currently before today's scheduled time
        if (now.isBefore(scheduledToday)) {
            return false
        }

        if (lastAction == 0L) {
            return true
        }

        val lastActionTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastAction), ZoneId.systemDefault())

        // If the action was taken or skipped for today's scheduled slot, do not notify again
        if (!lastActionTime.isBefore(scheduledToday)) {
            return false
        }

        return when (reminder.frequency) {
            "Daily" -> {
                now.toLocalDate().isAfter(lastActionTime.toLocalDate())
            }
            "Weekly" -> {
                ChronoUnit.DAYS.between(lastActionTime, now) >= 6
            }
            "Monthly" -> {
                now.year != lastActionTime.year || now.monthValue != lastActionTime.monthValue
            }
            else -> true
        }
    }

    private fun rescheduleAll(context: Context) {
        val db = AppDatabase.getDatabase(context)
        val scheduler = ReminderScheduler(context)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AutoClearPreferences.scheduleAutoClearWork(context)
                AutoClearPreferences.performAutoClear(context)

                val reminders = db.reminderDao().getAllReminders().first()
                reminders.filter { it.isEnabled }.forEach { reminder ->
                    scheduler.scheduleReminder(reminder)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, medicationName: String, pillCount: String, id: Int) {
        val channelId = "medication_reminder_channel"
        val notificationId = if (id != -1) id else 1

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_description)
            enableVibration(true)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takenIntent = Intent(context, ActionReceiver::class.java).apply {
            action = "ACTION_TAKEN"
            putExtra("REMINDER_ID", notificationId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 100_000,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissedIntent = Intent(context, ActionReceiver::class.java).apply {
            action = "ACTION_DISMISSED"
            putExtra("REMINDER_ID", notificationId)
        }
        val dismissedPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 200_000,
            dismissedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val config = context.resources.configuration
        val isBoxyScreen = config.screenWidthDp.toFloat() / config.screenHeightDp.toFloat() > 0.75f

        val title = if (isBoxyScreen) {
            context.getString(R.string.screen_title)
        } else {
            context.getString(R.string.notification_title)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.notification_content, pillCount, medicationName))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.notification_content, pillCount, medicationName)))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setDeleteIntent(dismissedPendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.mark_as_taken_button),
                takenPendingIntent
            )

        if (!isBoxyScreen) {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
        }

        notificationManager.notify(notificationId, builder.build())
    }
}
