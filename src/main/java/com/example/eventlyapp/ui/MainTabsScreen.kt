package com.example.eventlyapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.eventlyapp.model.NoteData
import com.example.eventlyapp.model.SubTaskData
import com.example.eventlyapp.model.TaskData

private enum class MainTab(val title: String) {
    HOME("Главная"),
    TASKS("Задачи"),
    NOTES("Записи")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(
    tasks: List<TaskData>,
    notes: List<NoteData>,
    sortByPriority: Boolean,
    onSortByPriorityChange: (Boolean) -> Unit,
    onAddTaskClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onTaskCheckedChange: (TaskData, Boolean) -> Unit,
    onSubTaskCheckedChange: (TaskData, SubTaskData, Boolean) -> Unit
) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        containerColor = Color(0xFFCCCCCC),
        topBar = {
            TopAppBar(
                title = { Text(currentTab.title) }
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
                MainTab.values().forEach { tab ->
                    Tab(
                        selected = tab == currentTab,
                        onClick = { currentTab = tab },
                        text = { Text(tab.title) }
                    )
                }
            }

            when (currentTab) {
                MainTab.HOME -> HomeScreen(
                    tasks = tasks,
                    notes = notes,
                    modifier = Modifier.fillMaxSize()
                )

                MainTab.TASKS -> TaskListScreen(
                    tasks = tasks,
                    sortByPriority = sortByPriority,
                    onSortByPriorityChange = onSortByPriorityChange,
                    onTaskCheckedChange = onTaskCheckedChange,
                    onSubTaskCheckedChange = onSubTaskCheckedChange,
                    modifier = Modifier.fillMaxSize()
                )

                MainTab.NOTES -> NotesListScreen(
                    notes = notes,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
