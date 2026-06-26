package com.example.eventlyapp.features.app.domain

import com.example.eventlyapp.features.notes.domain.model.NoteData
import com.example.eventlyapp.features.tasks.domain.model.TaskData

data class AppState(
    val currentScreen: AppScreen = AppScreen.MAIN_TABS,
    val currentMainTab: MainTab = MainTab.NEWS,
    val editingTask: TaskData? = null,
    val editingNote: NoteData? = null
)
