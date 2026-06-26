package com.example.eventlyapp.core.network

import com.example.eventlyapp.core.network.dto.NytNewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NytTopStoriesService {
    @GET("home.json")
    suspend fun getTopStories(
        @Query("api-key") apiKey: String
    ): NytNewsResponse
}
