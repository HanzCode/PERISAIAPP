package com.example.perisaiapps.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Palet Warna Kustom Anda
private val darkPurpleBlue = Color(0xFF120E26)
private val cardBackgroundColor = Color(0xFF1F1A38)
private val lightGrayPlaceholder = Color(0xFF4A4A5A)
private val buttonBackgroundColor = Color(0xFFD0D0D0)
private val buttonTextColor = Color(0xFF120E26)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val textColorAccent = Color(0xFFFDD835) // Kuning

// Skema Warna Gelap Kustom
private val AppDarkColorScheme = darkColorScheme(
    primary = textColorAccent,
    background = darkPurpleBlue,
    surface = darkPurpleBlue,
    onSurface = textColorPrimary,
    surfaceVariant = cardBackgroundColor,
    onSurfaceVariant = textColorSecondary,
    secondaryContainer = buttonBackgroundColor,
    onSecondaryContainer = buttonTextColor,
    onPrimary = darkPurpleBlue,
    tertiary = lightGrayPlaceholder
)

// Skema Warna Terang Default (bisa diabaikan untuk saat ini)
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun PerisaiAppsTheme(
    // PERUBAHAN UTAMA DI SINI:
    darkTheme: Boolean = true, // Selalu gunakan tema gelap
    dynamicColor: Boolean = false, // Matikan dynamic color agar tema kita tidak ditimpa
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> AppDarkColorScheme // <-- Sekarang akan selalu memilih ini
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}