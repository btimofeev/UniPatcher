/*
 Copyright (c) 2017-2021, 2026 Boris Timofeev

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
import org.emunix.unipatcher.Settings
import org.emunix.unipatcher.helpers.ResourceProvider
import org.emunix.unipatcher.tools.CreateXDelta3
import org.emunix.unipatcher.utils.FileUtils
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreatePatchViewModel @Inject constructor(
    private val settings: Settings,
    private val resourceProvider: ResourceProvider,
    private val fileUtils: FileUtils,
) : ViewModel() {

    private var sourceUri: Uri? = null
    private var modifiedUri: Uri? = null
    private var patchUri: Uri? = null

    private val _sourceName: MutableStateFlow<String> = MutableStateFlow("")
    val sourceName: StateFlow<String> = _sourceName.asStateFlow()

    private val _modifiedName: MutableStateFlow<String> = MutableStateFlow("")
    val modifiedName: StateFlow<String> = _modifiedName.asStateFlow()

    private val _patchName: MutableStateFlow<String> = MutableStateFlow("")
    val patchName: StateFlow<String> = _patchName.asStateFlow()

    private val _actionIsRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val actionIsRunning: StateFlow<Boolean> = _actionIsRunning.asStateFlow()

    private val _status: MutableStateFlow<Int?> = MutableStateFlow(null)
    val status: StateFlow<Int?> = _status.asStateFlow()

    private val _message: MutableSharedFlow<ActionResult> = MutableSharedFlow(extraBufferCapacity = 1)
    val message: SharedFlow<ActionResult> = _message.asSharedFlow()

    fun sourceSelected(uri: Uri) = viewModelScope.launch {
        sourceUri = uri
        _sourceName.value = fileUtils.getFileName(uri)
    }

    fun modifiedSelected(uri: Uri) = viewModelScope.launch {
        modifiedUri = uri
        _modifiedName.value = fileUtils.getFileName(uri)
    }

    fun patchSelected(uri: Uri) = viewModelScope.launch {
        patchUri = uri
        _patchName.value = fileUtils.getFileName(uri)
    }

    fun runActionClicked() = viewModelScope.launch {
        if (_actionIsRunning.value) return@launch
        when {
            sourceUri == null -> {
                _message.emit(ActionResult(
                    resourceProvider.getString(R.string.create_patch_fragment_toast_source_not_selected),
                    true,
                ))
                return@launch
            }
            modifiedUri == null -> {
                _message.emit(ActionResult(
                    resourceProvider.getString(R.string.create_patch_fragment_toast_modified_not_selected),
                    true,
                ))
                return@launch
            }
            patchUri == null -> {
                _message.emit(ActionResult(
                    resourceProvider.getString(R.string.create_patch_fragment_toast_patch_not_selected),
                    true,
                ))
                return@launch
            }
            else -> {
                try {
                    _actionIsRunning.value = true
                    createPatch()
                    _message.emit(ActionResult(
                        resourceProvider.getString(R.string.notify_create_patch_complete),
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

    private suspend fun createPatch() = withContext(Dispatchers.IO) {
        val sourceUri = sourceUri
        val modifiedUri = modifiedUri
        val patchUri = patchUri
        require(sourceUri != null) { "sourceUri is null" }
        require(modifiedUri != null) { "modifiedUri is null" }
        require(patchUri != null) { "patchUri is null" }

        _status.value = R.string.status_copying_files
        val sourceFile = fileUtils.copyToTempFile(sourceUri)
        val modifiedFile = fileUtils.copyToTempFile(modifiedUri)
        val patchFile = File.createTempFile("patch", ".xdelta", fileUtils.getTempDir())
        try {
            _status.value = R.string.status_creating_patch
            val patchMaker = CreateXDelta3(patchFile, sourceFile, modifiedFile, resourceProvider)
            patchMaker.create()
            _status.value = R.string.status_writing_result
            fileUtils.copy(patchFile, patchUri)
            settings.setPatchingSuccessful(true)
        } finally {
            fileUtils.delete(sourceFile)
            fileUtils.delete(modifiedFile)
            fileUtils.delete(patchFile)
        }
    }
}