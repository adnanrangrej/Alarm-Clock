package com.example.alarmclock.ui

import android.app.KeyguardManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarmclock.service.AlarmScheduler
import com.example.alarmclock.ui.theme.AlarmClockTheme
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmRingActivity : ComponentActivity() {
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var mediaPlayer: MediaPlayer

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use the newer APIs for showing above lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Acquire wake lock with timeout
        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager).run {
            newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,  // Use PARTIAL_WAKE_LOCK instead of FULL_WAKE_LOCK
                "AlarmClock:AlarmWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L) // 10 minutes
            }
        }

        // Setup alarm sound
        mediaPlayer = MediaPlayer().apply {
            setDataSource(
                this@AlarmRingActivity,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            )
            isLooping = true
            prepare()
            start()
        }

        val alarmId = intent.getIntExtra("ALARM_ID", 0)
        val alarmTime = LocalTime.now()

        // Dismiss keyguard programmatically
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(
                this,
                object : KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissSucceeded() {
                        super.onDismissSucceeded()
                    }
                }
            )
        }

        setContent {
            AlarmClockTheme {
                AlarmRingScreen(
                    time = alarmTime,
                    onDismiss = { finishAndRemoveTask() },
                    onSnooze = { snoozeAlarm(alarmId) }
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun snoozeAlarm(alarmId: Int) {
        // Reschedule alarm for 5 minutes later
        val scheduler = AlarmScheduler(this)
        val snoozeTime = LocalTime.now().plusMinutes(5)
        scheduler.scheduleSnooze(alarmId, snoozeTime)
        finishAndRemoveTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AlarmRingScreen(
    time: LocalTime,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    var showRipple by remember { mutableStateOf(true) }

    LaunchedEffect(showRipple) {
        delay(2000)
        showRipple = !showRipple
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Animated ripple effect
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = if (showRipple) 0.2f else 0f
                    ),
                    shape = CircleShape
                )
                .animateContentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Wake Up!",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onSnooze,
                    modifier = Modifier
                        .width(150.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Snooze",
                        fontSize = 18.sp
                    )
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .width(150.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Dismiss",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
} 