package com.example.eventlyapp.features.news.domain.model

import com.example.eventlyapp.core.id.HasId
import com.example.eventlyapp.core.id.Id

data class NewsArticleData(
    override val id: Id,
    val title: String,
    val abstractText: String,
    val source: String,
    val publishedAt: String,
    val imagePath: String?
) : HasId
