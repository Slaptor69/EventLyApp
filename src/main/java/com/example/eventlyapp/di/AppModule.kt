package com.example.eventlyapp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.eventlyapp.BuildConfig
import com.example.eventlyapp.EventlyApplication
import com.example.eventlyapp.features.app.data.local.PlannerDao
import com.example.eventlyapp.features.app.data.local.PlannerDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
object AppModule {
    @Provides
    @Singleton
    fun provideApplicationContext(application: EventlyApplication): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("evently_settings", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providePlannerDatabase(context: Context): PlannerDatabase {
        return Room.databaseBuilder(
            context,
            PlannerDatabase::class.java,
            "evently_planner.db"
        ).build()
    }

    @Provides
    @Singleton
    fun providePlannerDao(database: PlannerDatabase): PlannerDao {
        return database.plannerDao()
    }

    @Provides
    @Named(NYT_API_KEY)
    fun provideNytApiKey(): String {
        return BuildConfig.NYT_API_KEY
    }

    const val NYT_API_KEY = "nytApiKey"
}
