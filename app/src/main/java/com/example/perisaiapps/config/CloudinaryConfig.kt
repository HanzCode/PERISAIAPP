package com.example.perisaiapps.config

import android.content.Context
import com.cloudinary.android.MediaManager
import com.example.perisaiapps.BuildConfig // Pastikan import ini benar dan tidak merah

object CloudinaryConfig {
    fun setup(context: Context) {
        // Menambahkan tipe eksplisit Map<String, Any> untuk membantu compiler
        val config: Map<String, Any> = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        )
        MediaManager.init(context, config)
    }
}