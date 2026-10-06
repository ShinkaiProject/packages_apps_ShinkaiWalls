package com.shinkai.wallpapers.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Pure dynamic Material 3 theme that automatically derives colors from the user's active system
 * wallpaper (Monet) on Android 12+ (API 31+).
 */
@Composable
fun ShinkaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val colorScheme =
      when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val baseScheme =
              if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
          if (darkTheme) {
            // Material 3 Expressive brighter, rich tinted dark mode:
            // Lift surface tones so the stacked cards and surfaces are noticeably brighter
            baseScheme.copy(
                surfaceContainerLowest = baseScheme.surfaceContainer,
                surfaceContainerLow = baseScheme.surfaceContainerHigh,
                surfaceContainer = baseScheme.surfaceContainerHighest,
                surfaceContainerHigh = baseScheme.surfaceBright,
            )
          } else {
            // Material 3 Expressive crisp, radiant light mode:
            // Make the foreground stacked card crisp, clean, and bright
            baseScheme.copy(
                surfaceContainer = baseScheme.surfaceContainerLowest,
            )
          }
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
      }

  MaterialTheme(
      colorScheme = colorScheme,
      typography = ShinkaiTypography,
      content = content,
  )
}
