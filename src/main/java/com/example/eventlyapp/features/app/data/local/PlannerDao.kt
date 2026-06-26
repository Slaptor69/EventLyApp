package com.example.eventlyapp.features.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PlannerDao {
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY position ASC, title ASC")
    suspend fun getTasksWithSubTasks(): List<TaskWithSubTasks>

    @Query("SELECT * FROM notes ORDER BY position ASC, title ASC")
    suspend fun getNotes(): List<PlannerNoteEntity>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM tasks")
    suspend fun nextTaskPosition(): Int

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM notes")
    suspend fun nextNotePosition(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: PlannerTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubTasks(subtasks: List<PlannerSubTaskEntity>)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubTasksForTask(taskId: String)

    @Transaction
    suspend fun upsertTaskWithSubTasks(
        task: PlannerTaskEntity,
        subtasks: List<PlannerSubTaskEntity>
    ) {
        upsertTask(task)
        deleteSubTasksForTask(task.id)
        if (subtasks.isNotEmpty()) {
            upsertSubTasks(subtasks)
        }
    }

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: PlannerNoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)
}
