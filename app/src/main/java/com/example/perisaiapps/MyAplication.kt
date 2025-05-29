package com.example.perisaiapps

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Cloudinary MediaManager
        // Kita tidak perlu menyertakan API Key atau Secret di sini jika menggunakan unsigned upload.
        // Cukup init dengan application context. Cloud Name akan diset saat upload atau global.
        try {
            MediaManager.init(this)
            // Anda bisa juga set cloud name secara global jika mau:
            // val config = mapOf("cloud_name" to "NAMA_CLOUD_ANDA")
            // MediaManager.init(this, config)
        } catch (e: Exception) {
            // Handle error inisialisasi jika terjadi
            e.printStackTrace()
        }
    }
}