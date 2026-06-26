package com.example.eventlyapp.features.app.domain

import com.example.eventlyapp.core.elm.ElmUpdate
import com.example.eventlyapp.core.elm.toElmUpdate
import javax.inject.Inject

class AppReducer @Inject constructor() {
    fun update(
        state: AppState,
        msg: AppMsg
    ): ElmUpdate<AppState, Nothing, Nothing> {
        return when (msg) {
            is AppMsg.MainTabSelected -> state.copy(
                currentMainTab = msg.tab
            ).toElmUpdate()
            AppMsg.AddTaskClicked -> state.copy(
                currentScreen = AppScreen.CREATE_TASK,
                editingTask = null
            ).toElmUpdate()
            AppMsg.AddNoteClicked -> state.copy(
                currentScreen = AppScreen.CREATE_NOTE,
                editingNote = null
            ).toElmUpdate()
            AppMsg.SettingsClicked -> state.copy(
                currentScreen = AppScreen.SETTINGS
            ).toElmUpdate()
            AppMsg.CalendarClicked -> state.copy(
                currentScreen = AppScreen.TASK_CALENDAR
            ).toElmUpdate()
            is AppMsg.EditTaskClicked -> state.copy(
                currentScreen = AppScreen.EDIT_TASK,
                editingTask = msg.task
            ).toElmUpdate()
            is AppMsg.EditNoteClicked -> state.copy(
                currentScreen = AppScreen.EDIT_NOTE,
                editingNote = msg.note
            ).toElmUpdate()
            AppMsg.MainScreenRequested -> state.copy(
                currentScreen = AppScreen.MAIN_TABS,
                editingTask = null,
                editingNote = null
            ).toElmUpdate()
        }
    }
}
