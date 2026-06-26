package com.example.eventlyapp.features.calendar.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.eventlyapp.features.tasks.domain.model.TaskData
import com.example.eventlyapp.features.tasks.domain.model.dateKey
import com.example.eventlyapp.features.tasks.domain.model.deadlineDateKey
import com.example.eventlyapp.features.tasks.domain.model.isDeadlineOverdue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCalendarScreen(
    tasks: List<TaskData>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    val monthCalendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    var selectedDateKey by rememberSaveable { mutableStateOf(currentDateKey()) }
    val tasksByDate = tasks
        .mapNotNull { task -> task.deadlineDateKey()?.let { key -> key to task } }
        .groupBy(
            keySelector = { item -> item.first },
            valueTransform = { item -> item.second }
        )
    val selectedTasks = tasksByDate[selectedDateKey].orEmpty()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Календарь задач") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CalendarHeader(
                    monthCalendar = monthCalendar,
                    onPreviousMonthClick = { monthOffset -= 1 },
                    onNextMonthClick = { monthOffset += 1 }
                )
            }

            item {
                CalendarGrid(
                    monthCalendar = monthCalendar,
                    tasksByDate = tasksByDate,
                    selectedDateKey = selectedDateKey,
                    onDateClick = { key -> selectedDateKey = key }
                )
            }

            item {
                Text(
                    text = "Задачи на дату",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (selectedTasks.isEmpty()) {
                item {
                    Text(
                        text = "На выбранную дату дедлайнов нет",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(
                    items = selectedTasks,
                    key = { task -> task.id.value }
                ) { task ->
                    CalendarTaskCard(task = task)
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    monthCalendar: Calendar,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonthClick) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Предыдущий месяц"
            )
        }
        Text(
            text = SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(monthCalendar.time),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNextMonthClick) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Следующий месяц"
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    monthCalendar: Calendar,
    tasksByDate: Map<String, List<TaskData>>,
    selectedDateKey: String,
    onDateClick: (String) -> Unit
) {
    val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOffset = mondayBasedOffset(monthCalendar.get(Calendar.DAY_OF_WEEK))
    val cells = List(firstDayOffset) { null } + (1..daysInMonth).map { day -> day }
    val rows = cells.chunked(7)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        rows.forEach { rowDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowDays.forEach { day ->
                    if (day == null) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val key = dateKey(
                            year = monthCalendar.get(Calendar.YEAR),
                            month = monthCalendar.get(Calendar.MONTH) + 1,
                            day = day
                        )
                        CalendarDayCell(
                            day = day,
                            hasTasks = tasksByDate.containsKey(key),
                            isSelected = selectedDateKey == key,
                            onClick = { onDateClick(key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                repeat(7 - rowDays.size) {
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    hasTasks: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        hasTasks -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        hasTasks -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (hasTasks) FontWeight.Bold else FontWeight.Normal
                )
                if (hasTasks) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(contentColor)
                            .fillMaxWidth(0.28f)
                            .padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarTaskCard(
    task: TaskData
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (task.isFlagged) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Помечено флажком",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            Text(
                text = "Дедлайн: ${task.deadline.orEmpty()}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (task.isDeadlineOverdue()) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (task.description != null) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun mondayBasedOffset(dayOfWeek: Int): Int {
    return (dayOfWeek + 5) % 7
}

private fun currentDateKey(): String {
    val calendar = Calendar.getInstance()
    return dateKey(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1,
        day = calendar.get(Calendar.DAY_OF_MONTH)
    )
}
