package com.example.phase1.core

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


object DateTimeUtil{
    fun formatCurrentTime(currentTime:Long): String {
        val formatter = DateTimeFormatter.ofPattern("dd EEE yyyy", Locale.ENGLISH)
            .withZone(ZoneId.of("Asia/Karachi"))
        return formatter.format(Instant.ofEpochMilli(currentTime))
    }
}
