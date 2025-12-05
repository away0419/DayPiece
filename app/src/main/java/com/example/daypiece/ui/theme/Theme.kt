package com.example.daypiece.ui.theme

import android.app.Activity
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
    primary = DayRoutinePrimary,
    secondary = DayRoutineTextSecondaryDark,
    tertiary = SchedulePurple,
    background = DayRoutineBackgroundDark,
    surface = DayRoutineSurfaceDark,
    onPrimary = Color.White,
    onSecondary = DayRoutineTextPrimaryDark,
    onTertiary = Color.White,
    onBackground = DayRoutineTextPrimaryDark,
    onSurface = DayRoutineTextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = DayRoutinePrimary,
    secondary = DayRoutineTextSecondary,
    tertiary = SchedulePurple,
    background = DayRoutineBackground,
    surface = DayRoutineSurface,
    onPrimary = Color.White,
    onSecondary = DayRoutineTextPrimary,
    onTertiary = Color.White,
    onBackground = DayRoutineTextPrimary,
    onSurface = DayRoutineTextPrimary
)

@Composable
fun DayPieceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color는 토스 스타일을 위해 비활성화
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}