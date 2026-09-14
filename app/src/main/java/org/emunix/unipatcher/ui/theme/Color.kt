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

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val PrimaryLight = Color(0xFF795548)
val AccentLight = Color(0xFFFF5252)
val ActivityBackgroundLight = Color(0xFFFFFFFF)
val CardHeaderTextLight = Color(0xFF795548)
val CardLineLight = Color(0xFFE8E8E8)
val CardBackgroundLight = Color(0xFFFFFFFF)
val DrawerTextLight = Color(0x8A000000)
val DrawerSelectedTextLight = Color(0xFFF2777A)
val DrawerSelectorLight = Color(0xFFFFEBEE)
val ToolbarBackgroundLight = Color(0xFFFFFFFF)
val ToolbarTextLight = Color(0xFF000000)
val ToolbarBackArrowLight = Color(0xFF000000)
val DonateButtonLight = Color(0xFFE6921F)

val PrimaryDark = Color(0xFFF2777A)
val AccentDark = Color(0xFFF2777A)
val ActivityBackgroundDark = Color(0xFF2D2D2D)
val CardHeaderTextDark = Color(0xFFCCCCCC)
val CardLineDark = Color(0xFF4B4B4B)
val CardBackgroundDark = Color(0xFF393939)
val DrawerTextDark = Color(0xFFCCCCCC)
val DrawerSelectedTextDark = Color(0xFFE88E88)
val DrawerSelectorDark = Color(0x2CF3E5F5)
val ToolbarBackgroundDark = Color(0xFF393939)
val ToolbarTextDark = Color(0xFFCCCCCC)
val ToolbarBackArrowDark = Color(0xFFCCCCCC)
val DonateButtonDark = Color(0xFFD5A167)

data class ExtendedColors(
    val cardHeaderText: Color,
    val toolbarBackground: Color,
    val toolbarText: Color,
    val toolbarBackArrow: Color,
    val drawerText: Color,
    val drawerSelectedText: Color,
    val drawerSelector: Color,
    val donateButton: Color,
)

val LocalExtendedColors = staticCompositionLocalOf<ExtendedColors> {
    error("No ExtendedColors provided")
}
