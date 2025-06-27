package com.example.perisaiapps

import android.app.Application
import com.example.perisaiapps.config.CloudinaryConfig

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CloudinaryConfig.setup(this)

    }
}