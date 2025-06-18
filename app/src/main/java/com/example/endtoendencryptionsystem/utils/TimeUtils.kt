package com.example.endtoendencryptionsystem.utils

import java.util.Date

object TimeUtils {
    fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val todayStart = now / (1000 * 60 * 60 * 24) * (1000 * 60 * 60 * 24)
        val yesterdayStart = todayStart - 24 * 60 * 60 * 1000

        val date = Date(timestamp)

        return when {
            timestamp >= todayStart -> {
                // 今天
                android.text.format.DateFormat.format("HH:mm", date).toString()
            }
            timestamp >= yesterdayStart -> {
                // 昨天
                "昨天 ${android.text.format.DateFormat.format("HH:mm", date)}"
            }
            else -> {
                // 更久之前
                android.text.format.DateFormat.format("MM月dd日 HH:mm", date).toString()
            }
        }
    }
}
