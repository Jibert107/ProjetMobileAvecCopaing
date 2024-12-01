package com.example.kotlinmusic.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color



// Définir les color schemes pour le mode clair et sombre
private val DarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    background = SpotifyBlack,
    surface = SpotifyBlack,
    onPrimary = Color.White,
    onSurface = SpotifyGray,
    onBackground = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SpotifyGreen,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.Black,
    onSurface = SpotifyGray,
    onBackground = Color.Black
)

@Composable
fun KotlinMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Active le mode sombre automatiquement
    dynamicColor: Boolean = true,              // Active les couleurs dynamiques si disponibles
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Vérifie si les couleurs dynamiques sont disponibles (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Applique le thème à l'application
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Typographie personnalisée (si définie)
        content = content
    )
}
