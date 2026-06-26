package com.example.eventlyapp.features.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.eventlyapp.features.app.domain.AppMsg
import com.example.eventlyapp.features.app.domain.AppScreen
import com.example.eventlyapp.features.app.domain.PlannerMsg
import com.example.eventlyapp.features.news.presentation.NewsViewModel
import com.example.eventlyapp.features.notes.presentation.NoteCreateScreen
import com.example.eventlyapp.features.calendar.presentation.TaskCalendarScreen
import com.example.eventlyapp.features.settings.presentation.SettingsScreen
import com.example.eventlyapp.features.settings.presentation.SettingsViewModel
import com.example.eventlyapp.features.tasks.presentation.TaskCreateScreen

@Composable
fun EventlyAppRoot(
    appViewModel: AppViewModel,
    plannerViewModel: PlannerViewModel,
    newsViewModel: NewsViewModel,
    settingsViewModel: SettingsViewModel
) {
    val appState by appViewModel.state.collectAsState()
    val plannerState by plannerViewModel.state.collectAsState()

    when (appState.currentScreen) {
        AppScreen.MAIN_TABS -> MainTabsScreen(
            tasks = plannerState.tasks,
            visibleTasks = plannerState.visibleTasks,
            notes = plannerState.notes,
            newsViewModel = newsViewModel,
            currentTab = appState.currentMainTab,
            onTabChange = { tab -> appViewModel.accept(AppMsg.MainTabSelected(tab)) },
            taskSortMode = plannerState.taskSortMode,
            showFlaggedOnly = plannerState.showFlaggedOnly,
            onTaskSortModeChange = { sortMode ->
                plannerViewModel.accept(PlannerMsg.SetTaskSortMode(sortMode))
            },
            onShowFlaggedOnlyChange = { enabled ->
                plannerViewModel.accept(PlannerMsg.SetShowFlaggedOnly(enabled))
            },
            onAddTaskClick = { appViewModel.accept(AppMsg.AddTaskClicked) },
            onAddNoteClick = { appViewModel.accept(AppMsg.AddNoteClicked) },
            onSettingsClick = { appViewModel.accept(AppMsg.SettingsClicked) },
            onCalendarClick = { appViewModel.accept(AppMsg.CalendarClicked) },
            onTaskCompleteClick = { task ->
                plannerViewModel.accept(PlannerMsg.CompleteTaskRequested(task))
            },
            onTaskEditClick = { task -> appViewModel.accept(AppMsg.EditTaskClicked(task)) },
            onTaskDeleteClick = { task ->
                plannerViewModel.accept(PlannerMsg.DeleteTaskRequested(task))
            },
            onSubTaskCheckedChange = { task, subTask, isDone ->
                plannerViewModel.accept(PlannerMsg.SetSubTaskDone(task, subTask, isDone))
            },
            onNoteEditClick = { note -> appViewModel.accept(AppMsg.EditNoteClicked(note)) },
            onNoteDeleteClick = { note ->
                plannerViewModel.accept(PlannerMsg.DeleteNoteRequested(note))
            }
        )

        AppScreen.CREATE_TASK -> TaskCreateScreen(
            initialTask = null,
            onSave = { title, description, priority, isFlagged, deadline, subtasks ->
                plannerViewModel.accept(
                    PlannerMsg.AddTaskRequested(
                        title = title,
                        description = description,
                        priority = priority,
                        isFlagged = isFlagged,
                        deadline = deadline,
                        subtasks = subtasks
                    )
                )
                appViewModel.accept(AppMsg.MainScreenRequested)
            },
            onCancel = { appViewModel.accept(AppMsg.MainScreenRequested) }
        )

        AppScreen.EDIT_TASK -> TaskCreateScreen(
            initialTask = appState.editingTask,
            onSave = { title, description, priority, isFlagged, deadline, subtasks ->
                val taskToUpdate = appState.editingTask
                if (taskToUpdate != null) {
                    plannerViewModel.accept(
                        PlannerMsg.UpdateTaskRequested(
                            task = taskToUpdate,
                            title = title,
                            description = description,
                            priority = priority,
                            isFlagged = isFlagged,
                            deadline = deadline,
                            subtasks = subtasks
                        )
                    )
                }
                appViewModel.accept(AppMsg.MainScreenRequested)
            },
            onCancel = {
                appViewModel.accept(AppMsg.MainScreenRequested)
            }
        )

        AppScreen.CREATE_NOTE -> NoteCreateScreen(
            initialNote = null,
            onSave = { title, text ->
                plannerViewModel.accept(
                    PlannerMsg.AddNoteRequested(
                        title = title,
                        text = text
                    )
                )
                appViewModel.accept(AppMsg.MainScreenRequested)
            },
            onCancel = { appViewModel.accept(AppMsg.MainScreenRequested) }
        )

        AppScreen.EDIT_NOTE -> NoteCreateScreen(
            initialNote = appState.editingNote,
            onSave = { title, text ->
                val noteToUpdate = appState.editingNote
                if (noteToUpdate != null) {
                    plannerViewModel.accept(
                        PlannerMsg.UpdateNoteRequested(
                            note = noteToUpdate,
                            title = title,
                            text = text
                        )
                    )
                }
                appViewModel.accept(AppMsg.MainScreenRequested)
            },
            onCancel = {
                appViewModel.accept(AppMsg.MainScreenRequested)
            }
        )

        AppScreen.TASK_CALENDAR -> TaskCalendarScreen(
            tasks = plannerState.tasks,
            onBackClick = { appViewModel.accept(AppMsg.MainScreenRequested) }
        )

        AppScreen.SETTINGS -> SettingsScreen(
            viewModel = settingsViewModel,
            onBackClick = { appViewModel.accept(AppMsg.MainScreenRequested) }
        )
    }
}
