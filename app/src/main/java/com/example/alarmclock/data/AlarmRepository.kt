package com.example.alarmclock.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.alarmclock.model.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlarmRepository(private val alarmDao: AlarmDao) {
    @RequiresApi(Build.VERSION_CODES.O)
    val alarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()
        .map { entities -> entities.map { it.toAlarm() } }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addAlarm(alarm: Alarm) {
        alarmDao.insertAlarm(AlarmEntity.fromAlarm(alarm))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(AlarmEntity.fromAlarm(alarm))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(AlarmEntity.fromAlarm(alarm))
    }
} 