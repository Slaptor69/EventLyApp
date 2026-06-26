package com.example.eventlyapp.features.app.data

import com.example.eventlyapp.core.id.Id
import com.example.eventlyapp.features.app.data.local.PlannerNoteEntity
import com.example.eventlyapp.features.app.data.local.PlannerSubTaskEntity
import com.example.eventlyapp.features.app.data.local.PlannerTaskEntity
import com.example.eventlyapp.features.app.data.local.TaskWithSubTasks
import com.example.eventlyapp.features.notes.domain.model.NoteData
import com.example.eventlyapp.features.tasks.domain.model.SubTaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskPriority

fun TaskData.toTaskEntity(position: Int): PlannerTaskEntity {
    return PlannerTaskEntity(
        id = id.value,
        title = title,
        description = description,
        priority = priority.name,
        isFlagged = isFlagged,
        deadline = deadline,
        isDone = isDone,
        position = position
    )
}

fun TaskData.toSubTaskEntities(): List<PlannerSubTaskEntity> {
    return subtasks.mapIndexed { index, subTask ->
        PlannerSubTaskEntity(
            id = subTask.id.value,
            taskId = id.value,
            title = subTask.title,
            isDone = subTask.isDone,
            position = index
        )
    }
}

fun TaskWithSubTasks.toTaskData(): TaskData {
    return TaskData(
        id = Id(task.id),
        title = task.title,
        description = task.description,
        priority = runCatching { TaskPriority.valueOf(task.priority) }.getOrDefault(TaskPriority.MEDIUM),
        isFlagged = task.isFlagged,
        deadline = task.deadline,
        isDone = task.isDone,
        subtasks = subtasks
            .sortedWith(compareBy<PlannerSubTaskEntity> { entity -> entity.position }.thenBy { entity -> entity.title })
            .map { subTask ->
                SubTaskData(
                    id = Id(subTask.id),
                    title = subTask.title,
                    isDone = subTask.isDone
                )
            }
    )
}

fun NoteData.toNoteEntity(position: Int): PlannerNoteEntity {
    return PlannerNoteEntity(
        id = id.value,
        title = title,
        text = text,
        position = position
    )
}

fun PlannerNoteEntity.toNoteData(): NoteData {
    return NoteData(
        id = Id(id),
        title = title,
        text = text
    )
}
