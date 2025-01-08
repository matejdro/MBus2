package com.matejdro.mbus.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
   primary = md_theme_light_primary,
   onPrimary = md_theme_light_onPrimary,
   primaryContainer = md_theme_light_primaryContainer,
   onPrimaryContainer = md_theme_light_onPrimaryContainer,
   secondary = md_theme_light_secondary,
   onSecondary = md_theme_light_onSecondary,
   secondaryContainer = md_theme_light_secondaryContainer,
   onSecondaryContainer = md_theme_light_onSecondaryContainer,
   tertiary = md_theme_light_tertiary,
   onTertiary = md_theme_light_onTertiary,
   tertiaryContainer = md_theme_light_tertiaryContainer,
   onTertiaryContainer = md_theme_light_onTertiaryContainer,
   error = md_theme_light_error,
   errorContainer = md_theme_light_errorContainer,
   onError = md_theme_light_onError,
   onErrorContainer = md_theme_light_onErrorContainer,
   background = md_theme_light_background,
   onBackground = md_theme_light_onBackground,
   surface = md_theme_light_surface,
   onSurface = md_theme_light_onSurface,
   surfaceVariant = md_theme_light_surfaceVariant,
   onSurfaceVariant = md_theme_light_onSurfaceVariant,
   outline = md_theme_light_outline,
   inverseOnSurface = md_theme_light_inverseOnSurface,
   inverseSurface = md_theme_light_inverseSurface,
   inversePrimary = md_theme_light_inversePrimary,
   surfaceTint = md_theme_light_surfaceTint,
   outlineVariant = md_theme_light_outlineVariant,
   scrim = md_theme_light_scrim,
)

private val DarkColors = darkColorScheme(
   primary = md_theme_dark_primary,
   onPrimary = md_theme_dark_onPrimary,
   primaryContainer = md_theme_dark_primaryContainer,
   onPrimaryContainer = md_theme_dark_onPrimaryContainer,
   secondary = md_theme_dark_secondary,
   onSecondary = md_theme_dark_onSecondary,
   secondaryContainer = md_theme_dark_secondaryContainer,
   onSecondaryContainer = md_theme_dark_onSecondaryContainer,
   tertiary = md_theme_dark_tertiary,
   onTertiary = md_theme_dark_onTertiary,
   tertiaryContainer = md_theme_dark_tertiaryContainer,
   onTertiaryContainer = md_theme_dark_onTertiaryContainer,
   error = md_theme_dark_error,
   errorContainer = md_theme_dark_errorContainer,
   onError = md_theme_dark_onError,
   onErrorContainer = md_theme_dark_onErrorContainer,
   background = md_theme_dark_background,
   onBackground = md_theme_dark_onBackground,
   surface = md_theme_dark_surface,
   onSurface = md_theme_dark_onSurface,
   surfaceVariant = md_theme_dark_surfaceVariant,
   onSurfaceVariant = md_theme_dark_onSurfaceVariant,
   outline = md_theme_dark_outline,
   inverseOnSurface = md_theme_dark_inverseOnSurface,
   inverseSurface = md_theme_dark_inverseSurface,
   inversePrimary = md_theme_dark_inversePrimary,
   surfaceTint = md_theme_dark_surfaceTint,
   outlineVariant = md_theme_dark_outlineVariant,
   scrim = md_theme_dark_scrim,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MBusTheme(
   darkTheme: Boolean = isSystemInDarkTheme(),
   // Dynamic color is available on Android 12+
   dynamicColor: Boolean = true,
   content: @Composable () -> Unit,
) {
   val colorScheme = when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
         val context = LocalContext.current
         if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColors
      else -> LightColors
   }

   MaterialTheme(
      colorScheme = colorScheme,
      typography = MyTypography,
   ) {
      if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
         // Workaround for https://issuetracker.google.com/issues/274471576 - Ripple effects do not work on Android 13
         val defaultRippleConfig = LocalRippleConfiguration.current ?: RippleConfiguration()
         val transparencyFixRippleConfiguration = RippleConfiguration(
            color = defaultRippleConfig.color,
            rippleAlpha = defaultRippleConfig.rippleAlpha?.run {
               RippleAlpha(
                  draggedAlpha,
                  focusedAlpha,
                  hoveredAlpha,
                  pressedAlpha.coerceAtLeast(MIN_RIPPLE_ALPHA_ON_TIRAMISU)
               )
            }

         )
         CompositionLocalProvider(LocalRippleConfiguration provides transparencyFixRippleConfiguration) {
            content()
         }
      } else {
         content()
      }
   }
}

private const val MIN_RIPPLE_ALPHA_ON_TIRAMISU = 0.5f
