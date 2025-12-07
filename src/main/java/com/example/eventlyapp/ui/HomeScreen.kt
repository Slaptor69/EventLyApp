package com.example.eventlyapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventlyapp.model.NoteData
import com.example.eventlyapp.model.TaskData
import com.example.eventlyapp.model.TaskPriority

@Composable
fun HomeScreen(
    tasks: List<TaskData>,
    notes: List<NoteData>,
    modifier: Modifier = Modifier
) {
    val totalTasks = tasks.size
    val doneTasks = tasks.count { task -> task.isDone }
    val flaggedTasks = tasks.count { task -> task.isFlagged }
    val highPriorityTasks = tasks.count { task -> task.priority == TaskPriority.HIGH }
    val notesCount = notes.size
    val totalSubtasks = tasks.sumOf { task -> task.subtasks.size }
    val doneSubtasks = tasks.sumOf { task -> task.subtasks.count { sub -> sub.isDone } }
    val nextDeadlineTask = tasks
        .filter { task -> task.deadline != null && task.isDone == false }
        .minByOrNull { task -> task.deadline.orEmpty() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Всего задач: $totalTasks",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Выполнено задач: $doneTasks",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Отмечено флажком: $flaggedTasks",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "С высоким приоритетом: $highPriorityTasks",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Всего подзадач: $totalSubtasks",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Выполнено подзадач: $doneSubtasks",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Всего записей: $notesCount",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (nextDeadlineTask != null) {
                    Text(
                        text = "Ближайший дедлайн:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nextDeadlineTask.title,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (nextDeadlineTask.deadline != null) {
                        Text(
                            text = "Когда: ${nextDeadlineTask.deadline}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Text(
                        text = "Активных дедлайнов нет",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
