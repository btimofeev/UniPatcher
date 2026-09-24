/*
 Copyright (c) 2014-2021, 2026 Boris Timofeev

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

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.emunix.unipatcher.R
import org.emunix.unipatcher.helpers.ResourceProvider
import org.emunix.unipatcher.tools.SmdFixChecksum
import org.emunix.unipatcher.utils.FileUtils
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SmdFixChecksumViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val fileUtils: FileUtils,
) : ViewModel() {

    private var romUri: Uri? = null

    private val _romName: MutableStateFlow<String> = MutableStateFlow("")
    val romName: StateFlow<String> = _romName.asStateFlow()

    private val _actionIsRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val actionIsRunning: StateFlow<Boolean> = _actionIsRunning.asStateFlow()

    private val _status: MutableStateFlow<Int?> = MutableStateFlow(null)
    val status: StateFlow<Int?> = _status.asStateFlow()

    private val _message: MutableSharedFlow<ActionResult> = MutableSharedFlow(extraBufferCapacity = 1)
    val message: SharedFlow<ActionResult> = _message.asSharedFlow()

    fun romSelected(uri: Uri) = viewModelScope.launch {
        romUri = uri
        _romName.value = fileUtils.getFileName(uri)
    }

    fun runActionClicked() = viewModelScope.launch {
        if (_actionIsRunning.value) return@launch
        when (romUri) {
            null -> {
                _message.emit(ActionResult(
                    resourceProvider.getString(R.string.main_activity_toast_rom_not_selected),
                    true,
                ))
                return@launch
            }
            else -> {
                try {
                    _actionIsRunning.value = true
                    fixChecksum()
                    _message.emit(ActionResult(
                        resourceProvider.getString(R.string.notify_smd_fix_checksum_complete),
                        false,
                    ))
                } catch (e: Exception) {
                    val errorMsg =
                        e.message ?: resourceProvider.getString(
                            R.string.notify_error_unknown
                        )
                    _message.emit(ActionResult(errorMsg, true))
                } finally {
                    _actionIsRunning.value = false
                    _status.value = null
                }
            }
        }
    }

    private suspend fun fixChecksum() = withContext(Dispatchers.IO) {
        val romUri = romUri
        require(romUri != null) { "romUri is null" }

        var tmpFile: File? = null
        try {
            _status.value = R.string.status_copying_files
            tmpFile = fileUtils.copyToTempFile(romUri)
            _status.value = R.string.status_fixing_checksum
            val worker = SmdFixChecksum(tmpFile, resourceProvider, fileUtils)
            worker.fixChecksum()
            _status.value = R.string.status_writing_result
            fileUtils.copy(tmpFile, romUri)
        } finally {
            fileUtils.delete(tmpFile)
        }
    }
}