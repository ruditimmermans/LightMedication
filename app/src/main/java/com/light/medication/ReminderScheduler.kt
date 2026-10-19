package com.light.medication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.light.medication.data.Reminder
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(reminder: Reminder, forceNext: Boolean = false) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
            putExtra("MEDICATION_NAME", reminder.medicationName)
            putExtra("PILL_COUNT", reminder.pillCount)
            putExtra("HOUR", reminder.hour)
            putExtra("MINUTE", reminder.minute)
            putExtra("FREQUENCY", reminder.frequency)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var scheduledTime = now.withHour(reminder.hour)
            .withMinute(reminder.minute)
            .withSecond(0)
            .withNano(0)

        // Ensure the alarm is set in the future
        if (!scheduledTime.isAfter(now) || forceNext) {
            scheduledTime = when (reminder.frequency) {
                "Daily" -> scheduledTime.plusDays(1)
                "Weekly" -> scheduledTime.plusWeeks(1)
                "Monthly" -> scheduledTime.plusMonths(1)
                else -> scheduledTime.plusDays(1)
            }
        }

        val triggerTime = scheduledTime.toInstant().toEpochMilli()

        if (alarmManager.canScheduleExactAlarms()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (_: SecurityException) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelReminder(reminder: Reminder) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
