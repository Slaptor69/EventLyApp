package com.example.eventlyapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventlyapp.model.SubTaskData
import com.example.eventlyapp.model.TaskData
import com.example.eventlyapp.model.TaskPriority

@Composable
fun TaskListScreen(
    tasks: List<TaskData>,
    sortByPriority: Boolean,
    onSortByPriorityChange: (Boolean) -> Unit,
    onTaskCheckedChange: (TaskData, Boolean) -> Unit,
    onSubTaskCheckedChange: (TaskData, SubTaskData, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Сортировать по приоритету")
            Switch(
                checked = sortByPriority,
                onCheckedChange = { value -> onSortByPriorityChange(value) }
            )
        }

        if (tasks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Пока нет задач. Нажмите +, чтобы добавить первую.")
            }
        } else {
            val grouped = tasks.groupBy { task -> task.priority }
            val priorityOrder = listOf(TaskPriority.HIGH, TaskPriority.MEDIUM, TaskPriority.LOW)

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                priorityOrder.forEach { priority ->
                    val groupTasks = grouped[priority]
                    if (groupTasks != null && groupTasks.isNotEmpty()) {
                        item {
                            TaskGroupHeader(priority = priority)
                        }
                        items(groupTasks) { task ->
                            TaskCard(
                                task = task,
                                onCheckedChange = { checked -> onTaskCheckedChange(task, checked) },
                                onSubTaskCheckedChange = { sub, checked ->
                                    onSubTaskCheckedChange(task, sub, checked)
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskGroupHeader(priority: TaskPriority) {
    Text(
        text = when (priority) {
            TaskPriority.HIGH -> "Высокий приоритет"
            TaskPriority.MEDIUM -> "Средний приоритет"
            TaskPriority.LOW -> "Низкий приоритет"
        },
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun TaskCard(
    task: TaskData,
    onCheckedChange: (Boolean) -> Unit,
    onSubTaskCheckedChange: (SubTaskData, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (task.description != null && task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Приоритет: ${task.priority.title}",
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (task.isFlagged) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Помечено флажком",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    if (task.deadline != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Дедлайн: ${task.deadline}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Checkbox(
                    checked = task.isDone,
                    onCheckedChange = onCheckedChange
                )
            }

            if (task.subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Подзадачи:",
                    style = MaterialTheme.typography.labelMedium
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    task.subtasks.forEach { sub ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = sub.isDone,
                                onCheckedChange = { checked ->
                                    onSubTaskCheckedChange(sub, checked)
                                }
                            )
                            Text(
                                text = sub.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
