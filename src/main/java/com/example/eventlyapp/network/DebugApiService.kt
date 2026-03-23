package com.example.eventlyapp.network

import com.example.eventlyapp.network.dto.DebugPostRequest
import com.example.eventlyapp.network.dto.DebugPostResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DebugApiService {
    @POST("posts")
    suspend fun sendDebugEvent(
        @Body request: DebugPostRequest,
        @Header("Content-Type") contentType: String = "application/json"
    ): DebugPostResponse
}