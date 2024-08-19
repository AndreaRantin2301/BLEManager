package com.andrearantin.blemanagerexample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppController : Application() {

    companion object {
        init {
            System.loadLibrary("blemanagerexample")
        }

    }

    override fun onCreate() {
        super.onCreate()
    }
}