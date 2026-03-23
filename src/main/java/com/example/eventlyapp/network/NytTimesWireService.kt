package com.example.eventlyapp.network

import com.example.eventlyapp.network.dto.NytNewsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NytTimesWireService {
    @GET("content/{source}/{section}.json")
    suspend fun getNews(
        @Path("source") source: String,
        @Path("section") section: String,
        @Query("api-key") apiKey: String,
        @Query("limit") limit: Int = 20
    ): NytNewsResponse
}