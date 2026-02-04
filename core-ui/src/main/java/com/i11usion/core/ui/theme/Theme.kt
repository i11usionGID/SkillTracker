package com.i11usion.core.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme()

private val LightColorScheme = lightColorScheme(
    primary = Orange400,
    onPrimary = White,
    primaryContainer = Orange300,
    primaryFixed = Orange100,
    primaryFixedDim = Orange200,
    secondary = Blue200,
    secondaryContainer = Blue100,
    tertiary = Green200,
    tertiaryContainer = Green100,
    surface = Gray100,
    surfaceBright = Gray200,
    onSurface = Black,
    surfaceTint = Gray300,
    surfaceVariant = Gray400,
    onSurfaceVariant = Gray500,
    error = Red300,
    errorContainer = Red100,
    onError = Red200,
    onErrorContainer = Red300,
)

@Composable
fun SkillTrackerTheme(
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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