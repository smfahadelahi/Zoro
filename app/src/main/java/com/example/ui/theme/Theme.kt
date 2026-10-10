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

private val ZoroDarkColorScheme = darkColorScheme(
  primary = ZoroCyan,
  onPrimary = Color(0xFF00363F),
  primaryContainer = Color(0xFF004D5A),
  onPrimaryContainer = Color(0xFF99F5FF),
  secondary = ZoroEmerald,
  onSecondary = Color(0xFF00391A),
  secondaryContainer = Color(0xFF005328),
  onSecondaryContainer = Color(0xFF7CFFB4),
  tertiary = ZoroViolet,
  onTertiary = Color(0xFF381E72),
  tertiaryContainer = Color(0xFF4F378B),
  onTertiaryContainer = Color(0xFFE8DDFF),
  background = ZoroBgDark,
  onBackground = ZoroTextPrimary,
  surface = ZoroSurfaceDark,
  onSurface = ZoroTextPrimary,
  surfaceVariant = ZoroSurfaceVariantDark,
  onSurfaceVariant = ZoroTextSecondary,
  outline = ZoroOutlineDark,
  error = ZoroRed
)

private val ZoroLightColorScheme = lightColorScheme(
  primary = Color(0xFF006877),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFA1EFFF),
  onPrimaryContainer = Color(0xFF001F25),
  secondary = Color(0xFF006C34),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF8FF7B1),
  onSecondaryContainer = Color(0xFF00210C),
  tertiary = Color(0xFF6750A4),
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFEADDFF),
  onTertiaryContainer = Color(0xFF21005D),
  background = ZoroBgLight,
  onBackground = ZoroTextPrimaryLight,
  surface = ZoroSurfaceLight,
  onSurface = ZoroTextPrimaryLight,
  surfaceVariant = ZoroSurfaceVariantLight,
  onSurfaceVariant = ZoroTextSecondaryLight,
  outline = ZoroOutlineLight,
  error = ZoroRed
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek obsidian neural dark mode
  dynamicColor: Boolean = false, // Keep intentional cyber-neural styling
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> ZoroDarkColorScheme
    else -> ZoroLightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
