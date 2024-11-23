package com.example.alarmclock.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.alarmclock.model.Alarm
import java.time.LocalTime

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean,
    val label: String,
    val days: String // Stored as comma-separated numbers
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toAlarm(): Alarm = Alarm(
        id = id,
        time = LocalTime.of(hour, minute),
        isEnabled = isEnabled,
        label = label,
        days = days.split(",").filter { it.isNotEmpty() }.map { it.toInt() }.toSet()
    )

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun fromAlarm(alarm: Alarm): AlarmEntity = AlarmEntity(
            id = alarm.id,
            hour = alarm.time.hour,
            minute = alarm.time.minute,
            isEnabled = alarm.isEnabled,
            label = alarm.label,
            days = alarm.days.joinToString(",")
        )
    }
} 