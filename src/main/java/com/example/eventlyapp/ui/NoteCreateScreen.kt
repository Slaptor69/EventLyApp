package com.example.eventlyapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCreateScreen(
    onSave: (title: String, text: String) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFCCCCCC),
        topBar = {
            TopAppBar(
                title = { Text("Новая запись") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { value -> title = value },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Заголовок*") },
                placeholder = { Text("Например: идеи на завтра") }
            )

            OutlinedTextField(
                value = text,
                onValueChange = { value -> text = value },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = { Text("Текст записи") },
                placeholder = { Text("Запишите свои мысли или план действий") },
                maxLines = 6
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }
                Button(
                    onClick = { onSave(title, text) },
                    enabled = title.isNotBlank() && text.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}
