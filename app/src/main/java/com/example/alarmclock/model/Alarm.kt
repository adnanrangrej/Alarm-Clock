package com.example.alarmclock.model

import java.time.LocalTime

data class Alarm(
    val id: Int = 0,
    val time: LocalTime,
    val isEnabled: Boolean = true,
    val label: String = "",
    val days: Set<Int> = emptySet() // 1 = Monday, 7 = Sunday
) 