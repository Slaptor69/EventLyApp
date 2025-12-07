package com.example.eventlyapp.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.eventlyapp.id.IdGenerator
import com.example.eventlyapp.model.SubTaskData
import com.example.eventlyapp.model.TaskPriority
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreateScreen(
    onSave: (
        title: String,
        description: String,
        priority: TaskPriority,
        isFlagged: Boolean,
        deadline: String?,
        subtasks: List<SubTaskData>
    ) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var isFlagged by remember { mutableStateOf(false) }
    var hasDeadline by remember { mutableStateOf(false) }
    var deadlineText by remember { mutableStateOf("") }

    var newSubTaskTitle by remember { mutableStateOf("") }
    val subtasksState = remember { mutableStateListOf<SubTaskData>() }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var year by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var day by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var hour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    fun openDateTimePicker() {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                year = selectedYear
                month = selectedMonth
                day = selectedDayOfMonth

                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        hour = selectedHour
                        minute = selectedMinute
                        deadlineText = String.format(
                            "%02d.%02d.%04d %02d:%02d",
                            day,
                            month + 1,
                            year,
                            hour,
                            minute
                        )
                    },
                    hour,
                    minute,
                    true
                )
                timePickerDialog.show()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    Scaffold(
        containerColor = Color(0xFFCCCCCC),
        topBar = {
            TopAppBar(
                title = { Text("Новая задача") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { value -> title = value },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название задачи*") },
                placeholder = { Text("Например: сходить в магазин") }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { value -> description = value },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                label = { Text("Описание (опционально)") },
                placeholder = { Text("Например: купить молоко, хлеб, сыр") },
                maxLines = 4
            )

            Text(text = "Приоритет")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.values().forEach { option ->
                    val selected = option == priority
                    val buttonModifier = Modifier.weight(1f)

                    if (selected) {
                        Button(
                            onClick = { priority = option },
                            modifier = buttonModifier
                        ) {
                            Text(option.title)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { priority = option },
                            modifier = buttonModifier
                        ) {
                            Text(option.title)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Пометить флажком")
                Switch(
                    checked = isFlagged,
                    onCheckedChange = { value -> isFlagged = value }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Выбрать дату завершения")
                Switch(
                    checked = hasDeadline,
                    onCheckedChange = { value ->
                        hasDeadline = value
                        if (value == false) {
                            deadlineText = ""
                        }
                    }
                )
            }

            if (hasDeadline) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = { openDateTimePicker() }) {
                        Text("Выбрать дату и время")
                    }
                    if (deadlineText.isNotBlank()) {
                        Text(text = "Выбрано: $deadlineText")
                    }
                }
            }

            Text(text = "Подзадачи")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newSubTaskTitle,
                    onValueChange = { value -> newSubTaskTitle = value },
                    modifier = Modifier.weight(1f),
                    label = { Text("Название подзадачи") }
                )
                Button(
                    onClick = {
                        if (newSubTaskTitle.isNotBlank()) {
                            subtasksState.add(
                                SubTaskData(
                                    id = IdGenerator.nextId(),
                                    title = newSubTaskTitle,
                                    isDone = false
                                )
                            )
                            newSubTaskTitle = ""
                        }
                    },
                    enabled = newSubTaskTitle.isNotBlank()
                ) {
                    Text("Добавить")
                }
            }

            if (subtasksState.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                ) {
                    items(subtasksState) { sub ->
                        Text(
                            text = "• ${sub.title}",
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }
                Button(
                    onClick = {
                        val deadlineToSave = if (hasDeadline && deadlineText.isNotBlank()) {
                            deadlineText
                        } else {
                            null
                        }
                        onSave(
                            title,
                            description,
                            priority,
                            isFlagged,
                            deadlineToSave,
                            subtasksState.toList()
                        )
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}
