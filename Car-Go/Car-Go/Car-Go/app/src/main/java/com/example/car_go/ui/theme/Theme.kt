package com.example.car_go.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.car_go.ui.theme.Typography

// Define a palette that feels clean and modern (like cars/tech)
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),    // Green for growth/freshness
    secondary = Color(0xFF90CAF9),  // Light Blue for tech/trust
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF388E3C),    // Deep Green
    secondary = Color(0xFF2196F3),  // Clear Blue
    background = Color(0xFFF0F0F0),
    surface = Color.White,
    onPrimary = Color.White
    /* other colors */
)

@Composable
fun CARGOTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(), // you can customize
        typography = Typography(),
        content = content
    )
}