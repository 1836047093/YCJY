package com.example.yjcy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YjcyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}