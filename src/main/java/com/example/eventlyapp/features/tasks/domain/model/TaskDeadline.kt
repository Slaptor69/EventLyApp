package com.example.eventlyapp.features.tasks.domain.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun TaskData.deadlineEpochMillis(): Long? {
    val deadlineValue = deadline ?: return null
    val parser = SimpleDateFormat(DEADLINE_PATTERN, Locale.getDefault()).apply {
        isLenient = false
    }
    return runCatching { parser.parse(deadlineValue)?.time }.getOrNull()
}

fun TaskData.deadlineDateKey(): String? {
    val deadlineMillis = deadlineEpochMillis() ?: return null
    return dateKeyFromMillis(deadlineMillis)
}

fun TaskData.isDeadlineOverdue(
    nowMillis: Long = System.currentTimeMillis()
): Boolean {
    val deadlineMillis = deadlineEpochMillis() ?: return false
    return deadlineMillis < nowMillis
}

fun dateKeyFromMillis(millis: Long): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = millis
    }
    return dateKey(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1,
        day = calendar.get(Calendar.DAY_OF_MONTH)
    )
}

fun dateKey(
    year: Int,
    month: Int,
    day: Int
): String {
    return "%04d-%02d-%02d".format(year, month, day)
}

private const val DEADLINE_PATTERN = "dd.MM.yyyy HH:mm"
