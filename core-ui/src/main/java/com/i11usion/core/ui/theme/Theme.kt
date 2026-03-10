package com.i11usion.core.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkOrange400,
    onPrimary = DarkBlack,
    primaryContainer = DarkOrange300,
    primaryFixed = DarkOrange100,
    primaryFixedDim = DarkOrange200,
    inversePrimary = DarkPurple100,
    secondary = DarkBlue200,
    secondaryContainer = DarkBlue100,
    tertiary = DarkGreen200,
    tertiaryContainer = DarkGreen100,
    surface = DarkGray100,
    surfaceBright = DarkGray200,
    surfaceVariant = DarkGray300,
    onSurface = DarkWhite,
    onSurfaceVariant = DarkGray500,
    surfaceTint = DarkGray400,
    error = DarkRed300,
    errorContainer = DarkRed100,
    onError = DarkRed200,
    onErrorContainer = DarkRed300,
)

private val LightColorScheme = lightColorScheme(
    primary = Orange400,
    onPrimary = White,
    primaryContainer = Orange300,
    primaryFixed = Orange100,
    primaryFixedDim = Orange200,
    inversePrimary = Purple100,
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
    darkTheme: Boolean = true,
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