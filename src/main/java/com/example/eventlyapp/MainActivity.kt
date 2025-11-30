package com.example.eventlyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.eventlyapp.id.IdGenerator
import com.example.eventlyapp.model.ReminderNoteData
import com.example.eventlyapp.ui.TaskCreateScreen
import com.example.eventlyapp.ui.TaskListScreen
import com.example.eventlyapp.ui.theme.EventLyAppTheme
import androidx.compose.ui.graphics.Color


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Gray

            ) {
                EventlyAppRoot()
            }
            }
        }
    }


// два экрана нашего приложения
private enum class Screen {
    LIST,
    CREATE
}

@Composable
fun EventlyAppRoot() {
    // какой экран сейчас показываем
    var currentScreen by remember { mutableStateOf(Screen.LIST) }

    // список задач в памяти
    val tasks = remember { mutableStateListOf<ReminderNoteData>() }

    when (currentScreen) {
        Screen.LIST -> TaskListScreen(
            tasks = tasks,
            onAddClick = { currentScreen = Screen.CREATE }
        )

        Screen.CREATE -> TaskCreateScreen(
            onSave = { text ->
                if (text.isNotBlank()) {
                    tasks.add(
                        ReminderNoteData(
                            text = text,
                            status = false,
                            id = IdGenerator.nextId()
                        )
                    )
                }
                currentScreen = Screen.LIST
            },
            onCancel = { currentScreen = Screen.LIST }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventlyAppPreview() {
    EventLyAppTheme {  // или EventLyAppTheme
        EventlyAppRoot()
    }
}
