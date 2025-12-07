package com.example.eventlyapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventlyapp.id.IdGenerator
import com.example.eventlyapp.model.NoteData
import com.example.eventlyapp.model.SubTaskData
import com.example.eventlyapp.model.TaskData
import com.example.eventlyapp.model.TaskPriority
import com.example.eventlyapp.ui.theme.EventLyAppTheme

private enum class Screen {
    MAIN_TABS,
    CREATE_TASK,
    CREATE_NOTE
}

@Composable
fun EventlyAppRoot() {
    var currentScreen by remember { mutableStateOf(Screen.MAIN_TABS) }
    val tasks: SnapshotStateList<TaskData> = remember { mutableStateListOf() }
    val notes: SnapshotStateList<NoteData> = remember { mutableStateListOf() }
    var sortByPriority by remember { mutableStateOf(false) }

    when (currentScreen) {
        Screen.MAIN_TABS -> {
            val visibleTasks: List<TaskData> =
                if (sortByPriority) {
                    tasks.sortedWith(
                        compareByDescending<TaskData> { task -> task.priority.order }
                            .thenBy { task -> task.title }
                    )
                } else {
                    tasks
                }

            MainTabsScreen(
                tasks = visibleTasks,
                notes = notes,
                sortByPriority = sortByPriority,
                onSortByPriorityChange = { value -> sortByPriority = value },
                onAddTaskClick = { currentScreen = Screen.CREATE_TASK },
                onAddNoteClick = { currentScreen = Screen.CREATE_NOTE },
                onTaskCheckedChange = { task, isDone ->
                    val index = tasks.indexOfFirst { existing -> existing.id == task.id }
                    if (index >= 0) {
                        tasks[index] = tasks[index].copy(isDone = isDone)
                    }
                },
                onSubTaskCheckedChange = { task, subTask, isDone ->
                    val taskIndex = tasks.indexOfFirst { existing -> existing.id == task.id }
                    if (taskIndex >= 0) {
                        val updatedSubtasks: List<SubTaskData> =
                            tasks[taskIndex].subtasks.map { current ->
                                if (current.id == subTask.id) {
                                    current.copy(isDone = isDone)
                                } else {
                                    current
                                }
                            }
                        tasks[taskIndex] = tasks[taskIndex].copy(subtasks = updatedSubtasks)
                    }
                }
            )
        }

        Screen.CREATE_TASK -> {
            TaskCreateScreen(
                onSave = { title, description, priority, isFlagged, deadline, subtasks ->
                    val descriptionToSave = description.takeIf { value -> value.isNotBlank() }
                    tasks.add(
                        TaskData(
                            id = IdGenerator.nextId(),
                            title = title,
                            description = descriptionToSave,
                            priority = priority,
                            isFlagged = isFlagged,
                            deadline = deadline,
                            isDone = false,
                            subtasks = subtasks
                        )
                    )
                    currentScreen = Screen.MAIN_TABS
                },
                onCancel = { currentScreen = Screen.MAIN_TABS }
            )
        }

        Screen.CREATE_NOTE -> {
            NoteCreateScreen(
                onSave = { title, text ->
                    notes.add(
                        NoteData(
                            id = IdGenerator.nextId(),
                            title = title,
                            text = text
                        )
                    )
                    currentScreen = Screen.MAIN_TABS
                },
                onCancel = { currentScreen = Screen.MAIN_TABS }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventlyAppPreview() {
    EventLyAppTheme {
        EventlyAppRoot()
    }
}
