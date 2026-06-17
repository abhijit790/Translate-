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

private val DarkColorScheme =
  darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF121214),
    surface = Color(0xFF1E1E22),
    onBackground = Color(0xFFF3F4F9),
    onSurface = Color(0xFFF3F4F9),
    primaryContainer = Color(0xFF005CBB),
    secondaryContainer = Color(0xFF44474E)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PolishPrimary,
    onPrimary = Color.White,
    primaryContainer = PolishBadgeBg,
    onPrimaryContainer = PolishBadgeText,
    secondary = PolishPrimaryLabel,
    secondaryContainer = PolishResultBg,
    onSecondaryContainer = PolishTextDark,
    background = PolishBg,
    onBackground = PolishTextDark,
    surface = PolishWhite,
    onSurface = PolishTextDark,
    surfaceVariant = PolishResultBg,
    onSurfaceVariant = PolishTextMedium,
    outline = PolishBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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
