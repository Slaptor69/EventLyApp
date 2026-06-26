package com.example.eventlyapp.features.news.domain

sealed interface NewsEffect {
    data class ShowRefreshError(val message: String) : NewsEffect
}
