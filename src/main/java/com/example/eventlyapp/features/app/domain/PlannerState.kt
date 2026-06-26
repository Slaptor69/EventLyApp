package com.example.eventlyapp.features.app.domain

import com.example.eventlyapp.features.notes.domain.model.NoteData
import com.example.eventlyapp.features.tasks.domain.model.TaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskSortMode
import com.example.eventlyapp.features.tasks.domain.model.deadlineEpochMillis

data class PlannerState(
    val tasks: List<TaskData> = emptyList(),
    val notes: List<NoteData> = emptyList(),
    val taskSortMode: TaskSortMode = TaskSortMode.NONE,
    val showFlaggedOnly: Boolean = false
) {
    val visibleTasks: List<TaskData>
        get() {
            val filteredTasks = if (showFlaggedOnly) {
                tasks.filter { task -> task.isFlagged }
            } else {
                tasks
            }

            return when (taskSortMode) {
                TaskSortMode.NONE -> filteredTasks
                TaskSortMode.PRIORITY -> filteredTasks.sortedWith(
                    compareByDescending<TaskData> { task -> task.priority.order }
                        .thenBy { task -> task.title }
                )
                TaskSortMode.DEADLINE -> filteredTasks.sortedWith(
                    compareBy<TaskData> { task -> task.deadlineEpochMillis() ?: Long.MAX_VALUE }
                        .thenByDescending { task -> task.priority.order }
                        .thenBy { task -> task.title }
                )
            }
        }
}
