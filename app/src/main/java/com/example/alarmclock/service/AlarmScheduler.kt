package com.example.alarmclock.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.alarmclock.model.Alarm
import java.time.LocalTime
import android.provider.Settings

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @RequiresApi(Build.VERSION_CODES.O)
    fun schedule(alarm: Alarm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Intent().also { intent ->
                intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(java.util.Calendar.HOUR_OF_DAY, alarm.time.hour)
            set(java.util.Calendar.MINUTE, alarm.time.minute)
            set(java.util.Calendar.SECOND, 0)
            
            if (timeInMillis <= System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }

        if (alarm.isEnabled) {
            val info = AlarmManager.AlarmClockInfo(
                calendar.timeInMillis,
                pendingIntent
            )
            alarmManager.setAlarmClock(info, pendingIntent)
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun cancel(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleSnooze(alarmId: Int, snoozeTime: LocalTime) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("IS_SNOOZE", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId + 1000, // Use different request code for snooze
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(java.util.Calendar.HOUR_OF_DAY, snoozeTime.hour)
            set(java.util.Calendar.MINUTE, snoozeTime.minute)
            set(java.util.Calendar.SECOND, 0)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
} 