/*
Copyright (C) 2016, 2017, 2019-2021, 2026 Boris Timofeev

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

package org.emunix.unipatcher

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.emunix.unipatcher.helpers.ThemeHelper
import javax.inject.Inject


interface Settings {

    fun getShowHelpButton(): Boolean

    fun setShowHelpButton(show: Boolean)

    fun getIgnoreChecksum(): Boolean

    fun setIgnoreChecksum(ignore: Boolean)

    fun setPatchingSuccessful(isSuccessful: Boolean)

    fun getPatchingSuccessful(): Boolean

    fun setDontShowDonateSnackbarCount(count: Int)

    fun getDontShowDonateSnackbarCount(): Int

    fun getTheme(): String

    fun setTheme(theme: String)

    fun getThemeFlow(): StateFlow<String>
}


class SettingsImpl @Inject constructor(private val prefs: SharedPreferences) : Settings {

    private val _theme: MutableStateFlow<String> = MutableStateFlow(getTheme())

    override fun getShowHelpButton(): Boolean {
        return prefs.getBoolean("show_how_to_use_app_button", true)
    }

    override fun setShowHelpButton(show: Boolean) {
        prefs.edit { putBoolean("show_how_to_use_app_button", show) }
    }

    override fun getIgnoreChecksum(): Boolean {
        return prefs.getBoolean("ignore_checksum", false)
    }

    override fun setIgnoreChecksum(ignore: Boolean) {
        prefs.edit { putBoolean("ignore_checksum", ignore) }
    }

    override fun setPatchingSuccessful(isSuccessful: Boolean) {
        prefs.edit { putBoolean("patching_successful", isSuccessful) }
    }

    override fun getPatchingSuccessful(): Boolean {
        return prefs.getBoolean("patching_successful", false)
    }

    override fun setDontShowDonateSnackbarCount(count: Int) {
        prefs.edit { putInt("dont_show_donate_snackbar", count) }
    }

    override fun getDontShowDonateSnackbarCount(): Int {
        return prefs.getInt("dont_show_donate_snackbar", 0)
    }

    override fun getTheme(): String {
        return prefs.getString(PREFERENCES_THEME_KEY, ThemeHelper.DEFAULT_MODE) ?: ThemeHelper.DEFAULT_MODE
    }

    override fun setTheme(theme: String) {
        prefs.edit { putString(PREFERENCES_THEME_KEY, theme) }
        _theme.value = theme
    }

    override fun getThemeFlow(): StateFlow<String> {
        return _theme.asStateFlow()
    }
}
