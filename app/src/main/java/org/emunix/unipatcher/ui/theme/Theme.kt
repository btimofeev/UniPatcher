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

private val LightRedScheme = lightColorScheme(
    primary = Color(0xFF8F4B3E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF9DED0),
    onPrimaryContainer = Color(0xFF35150D),
    secondary = Color(0xFF77574C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBD4),
    onSecondaryContainer = Color(0xFF2C150F),
    tertiary = Color(0xFF745A48),
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF4DED8),
    onSurfaceVariant = Color(0xFF53433F),
    outline = Color(0xFF857371),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkRedScheme = darkColorScheme(
    primary = Color(0xFFD9A28C),
    onPrimary = Color(0xFF512A1C),
    primaryContainer = Color(0xFF6B3E2C),
    onPrimaryContainer = Color(0xFFFFDACA),
    secondary = Color(0xFFCBA79A),
    onSecondary = Color(0xFF3E2A21),
    secondaryContainer = Color(0xFF574038),
    onSecondaryContainer = Color(0xFFF6DCD2),
    tertiary = Color(0xFFB7ACA4),
    onTertiary = Color(0xFF2D221B),
    background = Color(0xFF0E0E0E),
    onBackground = Color(0xFFF8F8F8),
    surface = Color(0xFF0E0E0E),
    onSurface = Color(0xFFF8F8F8),
    surfaceVariant = Color(0xFF52443E),
    onSurfaceVariant = Color(0xFFE0E0E0),
    outline = Color(0xFF9E8D86),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun UniPatcherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkRedScheme
        else -> LightRedScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}