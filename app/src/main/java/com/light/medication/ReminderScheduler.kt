package com.light.medication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.light.medication.data.Reminder
import java.util.Calendar

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

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            
            if (forceNext || timeInMillis <= System.currentTimeMillis()) {
                when (reminder.frequency) {
                    "Daily" -> add(Calendar.DAY_OF_YEAR, 1)
                    "Weekly" -> add(Calendar.WEEK_OF_YEAR, 1)
                    "Monthly" -> add(Calendar.MONTH, 1)
                    else -> add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            calendar.timeInMillis,
            pendingIntent
        )
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
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
