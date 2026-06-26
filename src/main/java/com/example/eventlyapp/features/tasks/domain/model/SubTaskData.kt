package com.example.eventlyapp.features.tasks.domain.model

import com.example.eventlyapp.core.id.HasId
import com.example.eventlyapp.core.id.Id

data class SubTaskData(
    override val id: Id,
    val title: String,
    val isDone: Boolean
) : HasId
