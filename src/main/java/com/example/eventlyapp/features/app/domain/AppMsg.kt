package com.example.eventlyapp.features.app.domain

import com.example.eventlyapp.features.notes.domain.model.NoteData
import com.example.eventlyapp.features.tasks.domain.model.TaskData

sealed interface AppMsg {
    data class MainTabSelected(val tab: MainTab) : AppMsg
    data object AddTaskClicked : AppMsg
    data object AddNoteClicked : AppMsg
    data object SettingsClicked : AppMsg
    data object CalendarClicked : AppMsg
    data class EditTaskClicked(val task: TaskData) : AppMsg
    data class EditNoteClicked(val note: NoteData) : AppMsg
    data object MainScreenRequested : AppMsg
}
