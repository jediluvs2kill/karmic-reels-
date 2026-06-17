package com.example.ui.theme

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
    primary = BentoPeachCoral,
    onPrimary = BentoAccentDark,
    primaryContainer = BentoPrimaryRust,
    onPrimaryContainer = Color.White,
    secondary = BentoLightCoral,
    onSecondary = BentoAccentDark,
    secondaryContainer = BentoMutedWarm,
    onSecondaryContainer = Color.White,
    tertiary = BentoTonalEarth,
    onTertiary = BentoTextPrimary,
    background = BentoTextPrimary,
    onBackground = BentoBackground,
    surface = BentoMutedWarm,
    onSurface = BentoBackground,
    surfaceVariant = BentoPrimaryRust,
    onSurfaceVariant = BentoPeachCoral,
    outline = BentoBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimaryRust,
    onPrimary = Color.White,
    primaryContainer = BentoPeachCoral,
    onPrimaryContainer = BentoAccentDark,
    secondary = BentoMutedWarm,
    onSecondary = Color.White,
    secondaryContainer = BentoLightCoral,
    onSecondaryContainer = BentoPrimaryRust,
    tertiary = BentoAccentDark,
    onTertiary = BentoPeachCoral,
    background = BentoBackground,
    onBackground = BentoTextPrimary,
    surface = Color.White,
    onSurface = BentoTextPrimary,
    surfaceVariant = BentoTonalEarth,
    onSurfaceVariant = BentoTextSecondary,
    outline = BentoBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force premium dark luxury theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }


  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
