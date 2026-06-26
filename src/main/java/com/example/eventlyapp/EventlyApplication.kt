package com.example.eventlyapp

import android.app.Application
import com.example.eventlyapp.di.AppComponent
import com.example.eventlyapp.di.DaggerAppComponent

class EventlyApplication : Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(this)
    }
}
