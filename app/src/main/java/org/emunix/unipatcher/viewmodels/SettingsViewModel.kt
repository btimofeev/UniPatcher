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

package org.emunix.unipatcher.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.emunix.unipatcher.Settings
import org.emunix.unipatcher.helpers.ThemeHelper
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: Settings,
) : ViewModel() {

    private val _showHelpButton: MutableStateFlow<Boolean> =
        MutableStateFlow(settings.getShowHelpButton())
    val showHelpButton: StateFlow<Boolean> = _showHelpButton.asStateFlow()

    private val _ignoreChecksum: MutableStateFlow<Boolean> =
        MutableStateFlow(settings.getIgnoreChecksum())
    val ignoreChecksum: StateFlow<Boolean> = _ignoreChecksum.asStateFlow()

    private val _theme: MutableStateFlow<String> = MutableStateFlow(settings.getTheme())
    val theme: StateFlow<String> = _theme.asStateFlow()

    fun setShowHelpButton(value: Boolean) {
        settings.setShowHelpButton(value)
        _showHelpButton.value = value
    }

    fun setIgnoreChecksum(value: Boolean) {
        settings.setIgnoreChecksum(value)
        _ignoreChecksum.value = value
    }

    fun setTheme(value: String) {
        settings.setTheme(value)
        _theme.value = value
        ThemeHelper.applyTheme(value)
    }
}