package com.example.eventlyapp.model

import com.example.eventlyapp.id.HasId
import com.example.eventlyapp.id.Id

data class SubTaskData(
    override val id: Id,
    val title: String,
    val isDone: Boolean
) : HasId
