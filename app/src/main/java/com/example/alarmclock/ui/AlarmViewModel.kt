package com.example.alarmclock.ui

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmclock.data.AlarmDatabase
import com.example.alarmclock.data.AlarmRepository
import com.example.alarmclock.model.Alarm
import com.example.alarmclock.service.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AlarmRepository
    private val alarmScheduler: AlarmScheduler
    val alarms: StateFlow<List<Alarm>>

    init {
        val database = AlarmDatabase.getDatabase(application)
        repository = AlarmRepository(database.alarmDao())
        alarmScheduler = AlarmScheduler(application)
        alarms = repository.alarms.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addAlarm(hour: Int, minute: Int) {
        viewModelScope.launch {
            val alarm = Alarm(
                time = LocalTime.of(hour, minute)
            )
            repository.addAlarm(alarm)
            alarmScheduler.schedule(alarm)
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updatedAlarm)
            if (updatedAlarm.isEnabled) {
                alarmScheduler.schedule(updatedAlarm)
            } else {
                alarmScheduler.cancel(updatedAlarm)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
            alarmScheduler.cancel(alarm)
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
            if (alarm.isEnabled) {
                alarmScheduler.schedule(alarm)
            }
        }
    }
} 