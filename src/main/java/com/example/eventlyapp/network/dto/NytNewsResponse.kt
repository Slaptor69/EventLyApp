package com.example.eventlyapp.network.dto

import com.google.gson.annotations.SerializedName

data class NytNewsResponse(
    @SerializedName("results")
    val results: List<NytNewsItemDto>?
)

data class NytNewsItemDto(
    @SerializedName("title")
    val title: String?,
    @SerializedName("abstract")
    val abstractText: String?,
    @SerializedName("source")
    val source: String?,
    @SerializedName("published_date")
    val publishedDate: String?,
    @SerializedName("multimedia")
    val multimedia: List<NytMultimediaDto>?
)

data class NytMultimediaDto(
    @SerializedName("url")
    val url: String?,
    @SerializedName("format")
    val format: String?
)