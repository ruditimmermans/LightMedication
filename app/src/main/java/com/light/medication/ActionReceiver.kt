package com.light.medication

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

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val reminders = db.reminderDao().getAllReminders().first()
                val reminder = reminders.find { it.id == reminderId }
                
                if (reminder != null) {
                    val updatedReminder = reminder.copy(lastTakenTimestamp = System.currentTimeMillis())
                    db.reminderDao().update(updatedReminder)
                }

                // Open the app to show the change
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(mainIntent)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
