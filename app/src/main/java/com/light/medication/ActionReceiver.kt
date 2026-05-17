package com.light.medication

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.light.medication.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("REMINDER_ID", -1)
        if (reminderId == -1) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(reminderId)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val reminders = db.reminderDao().getAllReminders().first()
                val reminder = reminders.find { it.id == reminderId }
                
                if (reminder != null) {
                    val updatedReminder = if (intent.action == "ACTION_DISMISSED") {
                        reminder.copy(lastSkippedTimestamp = System.currentTimeMillis())
                    } else {
                        reminder.copy(lastTakenTimestamp = System.currentTimeMillis())
                    }
                    db.reminderDao().update(updatedReminder)
                }

                // Only open the app if it was a click (not a dismissal)
                if (intent.action != "ACTION_DISMISSED") {
                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(mainIntent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
