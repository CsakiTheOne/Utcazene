package com.csakitheone.streetmusic.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.csakitheone.streetmusic.ui.components.util.PreferenceHolder

private val DarkColorScheme = darkColorScheme(
    primary = Yellow,
    secondary = Blue,
    tertiary = YellowDark,

    surface = Blue,
    onSurface = YellowLight,

    error = Rose,
    errorContainer = Rose,

    background = BlueDark,
    onBackground = YellowLight,
)

private val LightColorScheme = lightColorScheme(
    primary = YellowDark,
    secondary = Blue,
    tertiary = Yellow,

    surface = CyanDark,
    onSurface = CyanLight,

    error = Rose,
    onErrorContainer = Rose,

    background = YellowLight,
    onBackground = BlueDark,
)

private val BatterySaverColorScheme = darkColorScheme(
    primary = Yellow,
    secondary = Cyan,
    tertiary = Blue,
    surfaceVariant = Cyan,
    background = Color.Black,
)

@Composable
fun UtcazeneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    isTintingNavbar: Boolean = true,
    content: @Composable () -> Unit
) {
    var isBatterySaverOn by remember { mutableStateOf(false) }

    PreferenceHolder(
        id = "batterySaver",
        value = isBatterySaverOn,
        isValueChanged = { isBatterySaverOn = it },
        defaultValue = false,
    )
    
    val colorScheme = when {
        isBatterySaverOn -> BatterySaverColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme && !isBatterySaverOn
            if (isTintingNavbar) {
                window.navigationBarColor = colorScheme.background.toArgb()
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(topStart = 4.dp, topEnd = 2.dp, bottomStart = 2.dp, bottomEnd = 4.dp),
            small = RoundedCornerShape(topStart = 8.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 8.dp),
            medium = RoundedCornerShape(topStart = 16.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 16.dp),
            large = RoundedCornerShape(topStart = 24.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 24.dp),
            extraLarge = RoundedCornerShape(topStart = 32.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 32.dp),
        ),
    )
}