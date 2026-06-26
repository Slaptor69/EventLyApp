package com.example.eventlyapp.features.app.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.eventlyapp.features.app.domain.MainTab
import com.example.eventlyapp.features.home.presentation.HomeScreen
import com.example.eventlyapp.features.news.presentation.NewsFeedScreen
import com.example.eventlyapp.features.news.presentation.NewsViewModel
import com.example.eventlyapp.features.notes.domain.model.NoteData
import com.example.eventlyapp.features.notes.presentation.NotesListScreen
import com.example.eventlyapp.features.tasks.domain.model.SubTaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskData
import com.example.eventlyapp.features.tasks.domain.model.TaskSortMode
import com.example.eventlyapp.features.tasks.presentation.TaskListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(
    tasks: List<TaskData>,
    visibleTasks: List<TaskData>,
    notes: List<NoteData>,
    newsViewModel: NewsViewModel,
    currentTab: MainTab,
    onTabChange: (MainTab) -> Unit,
    taskSortMode: TaskSortMode,
    showFlaggedOnly: Boolean,
    onTaskSortModeChange: (TaskSortMode) -> Unit,
    onShowFlaggedOnlyChange: (Boolean) -> Unit,
    onAddTaskClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onTaskCompleteClick: (TaskData) -> Unit,
    onTaskEditClick: (TaskData) -> Unit,
    onTaskDeleteClick: (TaskData) -> Unit,
    onSubTaskCheckedChange: (TaskData, SubTaskData, Boolean) -> Unit,
    onNoteEditClick: (NoteData) -> Unit,
    onNoteDeleteClick: (NoteData) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(currentTab.title) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentTab == MainTab.TASKS) {
                FloatingActionButton(onClick = onAddTaskClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить задачу"
                    )
                }
            } else if (currentTab == MainTab.NOTES) {
                FloatingActionButton(onClick = onAddNoteClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Добавить запись"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = currentTab.ordinal) {
                MainTab.entries.forEach { tab ->
                    Tab(
                        selected = tab == currentTab,
                        onClick = { onTabChange(tab) },
                        text = { Text(tab.title) }
                    )
                }
            }

            when (currentTab) {
                MainTab.NEWS -> NewsFeedScreen(
                    viewModel = newsViewModel,
                    modifier = Modifier.fillMaxSize()
                )

                MainTab.HOME -> HomeScreen(
                    tasks = tasks,
                    notes = notes,
                    onCalendarClick = onCalendarClick,
                    modifier = Modifier.fillMaxSize()
                )

                MainTab.TASKS -> TaskListScreen(
                    tasks = visibleTasks,
                    taskSortMode = taskSortMode,
                    showFlaggedOnly = showFlaggedOnly,
                    onTaskSortModeChange = onTaskSortModeChange,
                    onShowFlaggedOnlyChange = onShowFlaggedOnlyChange,
                    onTaskCompleteClick = onTaskCompleteClick,
                    onTaskEditClick = onTaskEditClick,
                    onTaskDeleteClick = onTaskDeleteClick,
                    onSubTaskCheckedChange = onSubTaskCheckedChange,
                    modifier = Modifier.fillMaxSize()
                )

                MainTab.NOTES -> NotesListScreen(
                    notes = notes,
                    onNoteEditClick = onNoteEditClick,
                    onNoteDeleteClick = onNoteDeleteClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
