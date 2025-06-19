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

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)
private val darkPurpleBlue = Color(0xFF120E26)
private val cardBackgroundColor = Color(0xFF1F1A38)
private val lightGrayPlaceholder = Color(0xFF4A4A5A)
private val buttonBackgroundColor = Color(0xFFD0D0D0)
private val buttonTextColor = Color(0xFF120E26)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val textColorAccent = Color(0xFFFDD835) // Kuning

// Definisikan Skema Warna Gelap untuk Material 3
private val AppDarkColorScheme = darkColorScheme(
    primary = textColorAccent, // Aksen kuning sebagai primary
    background = darkPurpleBlue,
    surface = darkPurpleBlue,
    onSurface = textColorPrimary,
    surfaceVariant = cardBackgroundColor, // Warna kartu
    onSurfaceVariant = textColorSecondary,
    secondaryContainer = buttonBackgroundColor, // Warna tombol
    onSecondaryContainer = buttonTextColor, // Warna teks tombol
    onPrimary = darkPurpleBlue, // Teks di atas warna primary (kuning)
    tertiary = lightGrayPlaceholder // Bisa untuk divider atau placeholder
)

@Composable
fun PerisaiAppsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
@Composable
fun PerisaiAppsDarkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppDarkColorScheme,
        typography = Typography, // Gunakan Typography yang sudah ada
        content = content
    )
}