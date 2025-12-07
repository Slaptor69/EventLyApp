package com.example.eventlyapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventlyapp.model.NoteData

@Composable
fun NotesListScreen(
    notes: List<NoteData>,
    modifier: Modifier = Modifier
) {
    if (notes.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Пока нет записей. Нажмите +, чтобы добавить первую.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            items(notes) { note ->
                NoteCard(note = note)
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = note.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
