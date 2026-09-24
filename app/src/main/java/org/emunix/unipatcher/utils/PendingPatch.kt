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

package org.emunix.unipatcher.utils

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the patch file that was opened from the file manager via an ACTION_VIEW
 * intent, until it is picked up by the ApplyPatch screen.
 */
@Singleton
class PendingPatch @Inject constructor() {

    private val _patch = MutableStateFlow<Uri?>(null)
    val patch: StateFlow<Uri?> = _patch.asStateFlow()

    fun set(uri: Uri) {
        _patch.value = uri
    }

    fun clear() {
        _patch.value = null
    }
}