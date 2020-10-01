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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ActionIsRunningViewModel: ViewModel(){
    private val actionIsRunning: MutableLiveData<Boolean> = MutableLiveData()

    private var applyPatch = false
    private var createPatch = false
    private var fixChecksum = false
    private var removeSmc = false

    init {
        updateState()
    }

    fun get(): LiveData<Boolean> = actionIsRunning

    private fun updateState() {
        val result = applyPatch || createPatch || fixChecksum || removeSmc
        actionIsRunning.value = result
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