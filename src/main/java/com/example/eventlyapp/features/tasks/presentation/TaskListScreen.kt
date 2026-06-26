package com.example.eventlyapp.features.tasks.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.eventlyapp.features.tasks.domain.model.SubTaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskSortMode
import com.example.eventlyapp.features.tasks.domain.model.isDeadlineOverdue

@Composable
fun TaskListScreen(
    tasks: List<TaskData>,
    taskSortMode: TaskSortMode,
    showFlaggedOnly: Boolean,
    onTaskSortModeChange: (TaskSortMode) -> Unit,
    onShowFlaggedOnlyChange: (Boolean) -> Unit,
    onTaskCompleteClick: (TaskData) -> Unit,
    onTaskEditClick: (TaskData) -> Unit,
    onTaskDeleteClick: (TaskData) -> Unit,
    onSubTaskCheckedChange: (TaskData, SubTaskData, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("task-list")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Сортировать по")
                OutlinedButton(onClick = { showSortDialog = true }) {
                    Text(taskSortMode.title)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Только с флажком")
                Switch(
                    checked = showFlaggedOnly,
                    onCheckedChange = onShowFlaggedOnlyChange
                )
            }
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
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = tasks,
                    key = { task -> task.id.value }
                ) { task ->
                    TaskCard(
                        task = task,
                        onCompleteClick = { onTaskCompleteClick(task) },
                        onEditClick = { onTaskEditClick(task) },
                        onDeleteClick = { onTaskDeleteClick(task) },
                        onSubTaskCheckedChange = { sub, checked ->
                            onSubTaskCheckedChange(task, sub, checked)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showSortDialog) {
        SortDialog(
            selectedSortMode = taskSortMode,
            onSortModeClick = { sortMode ->
                onTaskSortModeChange(sortMode)
                showSortDialog = false
            },
            onDismissRequest = { showSortDialog = false }
        )
    }
}

@Composable
private fun SortDialog(
    selectedSortMode: TaskSortMode,
    onSortModeClick: (TaskSortMode) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Сортировать по") },
        text = {
            Column {
                TaskSortMode.entries.forEach { sortMode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortModeClick(sortMode) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sortMode.title)
                        if (sortMode == selectedSortMode) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Выбрано",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun TaskCard(
    task: TaskData,
    onCompleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSubTaskCheckedChange: (SubTaskData, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var completingSubTask by remember { mutableStateOf<SubTaskData?>(null) }
    val isOverdue = task.isDeadlineOverdue()

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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (task.deadline != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Дедлайн: ${task.deadline}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isOverdue) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
                if (task.isFlagged) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Помечено флажком",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }

            if (isOverdue) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Дедлайн просрочен",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = false,
                    onCheckedChange = { checked ->
                        if (checked) {
                            showCompleteDialog = true
                        }
                    }
                )
                Text(
                    text = "Вся задача выполнена",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (task.description != null && task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Приоритет: ${task.priority.title}",
                style = MaterialTheme.typography.labelMedium
            )

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
                                    if (checked) {
                                        completingSubTask = sub
                                    }
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

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать задачу"
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить задачу",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Завершить задачу?") },
            text = { Text("Вы уверены, что завершили эту задачу? Она исчезнет из списка.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCompleteDialog = false
                        onCompleteClick()
                    }
                ) {
                    Text("Завершить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Отменить")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить задачу?") },
            text = { Text("Вы уверены, что хотите удалить?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    if (completingSubTask != null) {
        AlertDialog(
            onDismissRequest = { completingSubTask = null },
            title = { Text("Завершить подзадачу?") },
            text = { Text("Вы уверены, что завершили эту подзадачу? Она исчезнет из списка.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val subTask = completingSubTask
                        completingSubTask = null
                        if (subTask != null) {
                            onSubTaskCheckedChange(subTask, true)
                        }
                    }
                ) {
                    Text("Завершить")
                }
            },
            dismissButton = {
                TextButton(onClick = { completingSubTask = null }) {
                    Text("Отменить")
                }
            }
        )
    }
}
