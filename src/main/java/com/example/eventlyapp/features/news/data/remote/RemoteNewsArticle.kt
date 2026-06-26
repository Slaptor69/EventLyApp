package com.example.eventlyapp.features.news.data.remote

data class RemoteNewsArticle(
    val title: String,
    val abstractText: String,
    val source: String,
    val publishedAt: String,
    val imageUrl: String?
)
