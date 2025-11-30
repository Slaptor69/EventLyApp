package com.example.eventlyapp.model

import com.example.eventlyapp.id.HasId
import com.example.eventlyapp.id.Id

data class ReminderNoteData(
    val text: String,
    val status: Boolean,
    override val id: Id
) : HasId
