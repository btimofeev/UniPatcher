/*
 Copyright (c) 2026 Boris Timofeev

 This file is part of UniPatcher.

 UniPatcher is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 UniPatcher is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with UniPatcher.  If not, see <http://www.gnu.org/licenses/>.

 */

package org.emunix.unipatcher.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = PrimaryLight,
    secondary = AccentLight,
    onSecondary = Color.White,
    secondaryContainer = AccentLight.copy(alpha = 0.12f),
    onSecondaryContainer = AccentLight,
    background = ActivityBackgroundLight,
    onBackground = Color(0xFF1C1B1F),
    surface = ActivityBackgroundLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = CardBackgroundLight,
    onSurfaceVariant = CardHeaderTextLight,
    outline = CardLineLight,
    error = AccentLight,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF381E1E),
    primaryContainer = PrimaryDark.copy(alpha = 0.12f),
    onPrimaryContainer = PrimaryDark,
    secondary = AccentDark,
    onSecondary = Color(0xFF381E1E),
    secondaryContainer = AccentDark.copy(alpha = 0.12f),
    onSecondaryContainer = AccentDark,
    background = ActivityBackgroundDark,
    onBackground = Color(0xFFE6E1E5),
    surface = ActivityBackgroundDark,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = CardBackgroundDark,
    onSurfaceVariant = CardHeaderTextDark,
    outline = CardLineDark,
    error = AccentDark,
    onError = Color(0xFF601410),
)

@Composable
fun UniPatcherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
        content = content,
    )
}
