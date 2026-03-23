package com.example.eventlyapp.model

import com.example.eventlyapp.id.HasId
import com.example.eventlyapp.id.Id

data class NewsArticleData(
    override val id: Id,
    val title: String,
    val abstractText: String,
    val source: String,
    val publishedAt: String,
    val imageUrl: String?
) : HasId