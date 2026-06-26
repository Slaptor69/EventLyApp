package com.example.eventlyapp.di

import com.example.eventlyapp.core.network.DebugApiService
import com.example.eventlyapp.core.network.NytTopStoriesService
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @Named(NYT_RETROFIT)
    fun provideNytRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.nytimes.com/svc/topstories/v2/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named(DEBUG_RETROFIT)
    fun provideDebugRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNytTopStoriesService(
        @Named(NYT_RETROFIT) retrofit: Retrofit
    ): NytTopStoriesService {
        return retrofit.create(NytTopStoriesService::class.java)
    }

    @Provides
    @Singleton
    fun provideDebugApiService(
        @Named(DEBUG_RETROFIT) retrofit: Retrofit
    ): DebugApiService {
        return retrofit.create(DebugApiService::class.java)
    }

    private const val NYT_RETROFIT = "nytRetrofit"
    private const val DEBUG_RETROFIT = "debugRetrofit"
}
