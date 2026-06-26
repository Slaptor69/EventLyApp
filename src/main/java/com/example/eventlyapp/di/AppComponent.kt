package com.example.eventlyapp.di

import com.example.eventlyapp.EventlyApplication
import com.example.eventlyapp.features.app.presentation.AppViewModelFactory
import com.example.eventlyapp.features.app.presentation.PlannerViewModelFactory
import com.example.eventlyapp.features.news.presentation.NewsViewModelFactory
import com.example.eventlyapp.features.settings.presentation.SettingsViewModelFactory
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, NetworkModule::class])
interface AppComponent {
    fun appViewModelFactory(): AppViewModelFactory
    fun plannerViewModelFactory(): PlannerViewModelFactory
    fun newsViewModelFactory(): NewsViewModelFactory
    fun settingsViewModelFactory(): SettingsViewModelFactory

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: EventlyApplication
        ): AppComponent
    }
}
