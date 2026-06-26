package com.example.eventlyapp.features.notes.domain.model

import com.example.eventlyapp.core.id.HasId
import com.example.eventlyapp.core.id.Id

data class NoteData(
    override val id: Id,
    val title: String,
    val text: String
) : HasId
