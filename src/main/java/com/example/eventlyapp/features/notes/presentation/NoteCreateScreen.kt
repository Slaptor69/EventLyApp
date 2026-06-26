package com.example.eventlyapp.features.notes.presentation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.eventlyapp.features.notes.domain.model.NoteData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCreateScreen(
    initialNote: NoteData? = null,
    onSave: (title: String, text: String) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember(initialNote) { mutableStateOf(initialNote?.title.orEmpty()) }
    var text by remember(initialNote) { mutableStateOf(initialNote?.text.orEmpty()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (initialNote == null) "Новая запись" else "Редактировать запись") }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отменить")
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
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
        }
    }
}
