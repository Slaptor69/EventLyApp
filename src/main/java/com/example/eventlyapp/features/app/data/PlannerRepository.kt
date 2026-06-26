package com.example.eventlyapp.features.app.data

import com.example.eventlyapp.features.app.data.local.PlannerDao
import com.example.eventlyapp.features.notes.domain.model.NoteData
import com.example.eventlyapp.features.tasks.domain.model.TaskData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlannerRepository @Inject constructor(
    private val plannerDao: PlannerDao
) {
    suspend fun loadSnapshot(): PlannerSnapshot = withContext(Dispatchers.IO) {
        PlannerSnapshot(
            tasks = plannerDao.getTasksWithSubTasks().map { task -> task.toTaskData() },
            notes = plannerDao.getNotes().map { note -> note.toNoteData() }
        )
    }

    suspend fun saveTask(task: TaskData) = withContext(Dispatchers.IO) {
        val existingTasks = plannerDao.getTasksWithSubTasks()
        val existingPosition = existingTasks.firstOrNull { current -> current.task.id == task.id.value }?.task?.position
        val position = existingPosition ?: plannerDao.nextTaskPosition()

        plannerDao.upsertTaskWithSubTasks(
            task = task.toTaskEntity(position),
            subtasks = task.toSubTaskEntities()
        )
    }

    suspend fun deleteTask(task: TaskData) = withContext(Dispatchers.IO) {
        plannerDao.deleteTask(task.id.value)
    }

    suspend fun saveNote(note: NoteData) = withContext(Dispatchers.IO) {
        val existingNotes = plannerDao.getNotes()
        val existingPosition = existingNotes.firstOrNull { current -> current.id == note.id.value }?.position
        val position = existingPosition ?: plannerDao.nextNotePosition()

        plannerDao.upsertNote(note.toNoteEntity(position))
    }

    suspend fun deleteNote(note: NoteData) = withContext(Dispatchers.IO) {
        plannerDao.deleteNote(note.id.value)
    }
}
