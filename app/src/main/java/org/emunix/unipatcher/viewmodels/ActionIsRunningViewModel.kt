/*
 Copyright (c) 2020 Boris Timofeev

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

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActionResult(val message: String, val isError: Boolean)

class ActionIsRunningViewModel : ViewModel() {
    private val _actionIsRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val actionIsRunning: StateFlow<Boolean> = _actionIsRunning.asStateFlow()

    private val _status: MutableStateFlow<Int?> = MutableStateFlow(null)
    val status: StateFlow<Int?> = _status.asStateFlow()

    private val _result: MutableStateFlow<ActionResult?> = MutableStateFlow(null)
    val result: StateFlow<ActionResult?> = _result.asStateFlow()

    private var applyPatch = false
    private var createPatch = false
    private var fixChecksum = false
    private var removeSmc = false

    init {
        updateState()
    }

    private fun updateState() {
        val result = applyPatch || createPatch || fixChecksum || removeSmc
        _actionIsRunning.value = result
    }

    fun setStatus(@StringRes statusRes: Int?) {
        _status.value = statusRes
    }

    fun setResult(result: ActionResult) {
        _result.value = result
    }

    fun clearResult() {
        _result.value = null
    }

    fun applyPatch(value: Boolean) {
        applyPatch = value
        updateState()
    }

    fun createPatch(value: Boolean) {
        createPatch = value
        updateState()
    }

    fun fixChecksum(value: Boolean) {
        fixChecksum = value
        updateState()
    }

    fun removeSmc(value: Boolean) {
        removeSmc = value
        updateState()
    }
}