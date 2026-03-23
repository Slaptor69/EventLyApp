package com.example.eventlyapp.network.dto

data class DebugPostRequest(
    val title: String,
    val body: String,
    val userId: Int
)

data class DebugPostResponse(
    val id: Int?,
    val title: String?,
    val body: String?,
    val userId: Int?
)