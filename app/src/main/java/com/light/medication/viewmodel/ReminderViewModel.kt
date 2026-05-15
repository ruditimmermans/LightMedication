package com.light.medication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.light.medication.ReminderScheduler
import com.light.medication.data.AppDatabase
import com.light.medication.data.Reminder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val reminderDao = AppDatabase.getDatabase(application).reminderDao()
    private val scheduler = ReminderScheduler(application)

    val allReminders: StateFlow<List<Reminder>> = reminderDao.getAllReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addReminder(medicationName: String, pillCount: String, hour: Int, minute: Int, frequency: String) {
        viewModelScope.launch {
            val reminder = Reminder(
                medicationName = medicationName,
                pillCount = pillCount,
                hour = hour,
                minute = minute,
                frequency = frequency
            )
            val id = reminderDao.insert(reminder)
            // Schedule the alarm using the ID to avoid collisions
            scheduler.scheduleReminder(reminder.copy(id = id.toInt()))
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderDao.delete(reminder)
            scheduler.cancelReminder(reminder)
        }
    }
    
    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            reminderDao.update(updated)
            if (updated.isEnabled) {
                scheduler.scheduleReminder(updated)
            } else {
                scheduler.cancelReminder(updated)
            }
        }
    }

    fun updateReminder(reminder: Reminder, medicationName: String, pillCount: String, hour: Int, minute: Int, frequency: String) {
        viewModelScope.launch {
            val updated = reminder.copy(
                medicationName = medicationName,
                pillCount = pillCount,
                hour = hour,
                minute = minute,
                frequency = frequency
            )
            reminderDao.update(updated)
            if (updated.isEnabled) {
                scheduler.scheduleReminder(updated)
            }
        }
    }
}
