/*
 Copyright (c) 2013-2021, 2026 Boris Timofeev

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
import android.system.ErrnoException
import android.system.OsConstants
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
import org.emunix.unipatcher.patcher.PatcherFactory
import org.emunix.unipatcher.utils.FileUtils
import org.emunix.unipatcher.utils.isArchive
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ApplyPatchViewModel @Inject constructor(
    private val settings: Settings,
    private val resourceProvider: ResourceProvider,
    private val patcherFactory: PatcherFactory,
    private val fileUtils: FileUtils,
) : ViewModel() {

    private var patchUri: Uri? = null
    private var romUri: Uri? = null
    private var outputUri: Uri? = null

    private val _patchName: MutableStateFlow<String> = MutableStateFlow("")
    val patchName: StateFlow<String> = _patchName.asStateFlow()

    private val _romName: MutableStateFlow<String> = MutableStateFlow("")
    val romName: StateFlow<String> = _romName.asStateFlow()

    private val _outputName: MutableStateFlow<String> = MutableStateFlow("")
    val outputName: StateFlow<String> = _outputName.asStateFlow()

    private val _suggestedOutputName: MutableStateFlow<String> = MutableStateFlow("")
    val suggestedOutputName: StateFlow<String> = _suggestedOutputName.asStateFlow()

    private val _showHelpButton: MutableStateFlow<Boolean> =
        MutableStateFlow(settings.getShowHelpButton())
    val showHelpButton: StateFlow<Boolean> = _showHelpButton.asStateFlow()

    private val _actionIsRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val actionIsRunning: StateFlow<Boolean> = _actionIsRunning.asStateFlow()

    private val _status: MutableStateFlow<Int?> = MutableStateFlow(null)
    val status: StateFlow<Int?> = _status.asStateFlow()

    private val _message: MutableSharedFlow<ActionResult> = MutableSharedFlow(extraBufferCapacity = 1)
    val message: SharedFlow<ActionResult> = _message.asSharedFlow()

    fun patchSelected(uri: Uri) = viewModelScope.launch {
        patchUri = uri
        val name = fileUtils.getFileName(uri)
        _patchName.value = name
        checkArchive(name)
    }

    fun romSelected(uri: Uri) = viewModelScope.launch {
        romUri = uri
        val name = fileUtils.getFileName(uri)
        _romName.value = name
        checkArchive(name)
        suggestOutputName(name)
    }

    fun outputSelected(uri: Uri) = viewModelScope.launch {
        outputUri = uri
        _outputName.value = fileUtils.getFileName(uri)
    }

    fun refreshSettings() {
        _showHelpButton.value = settings.getShowHelpButton()
    }

    fun runActionClicked() = viewModelScope.launch {
        if (_actionIsRunning.value) return@launch
        when {
            patchUri == null -> {
                _message.emit(ActionResult(
                    resourceProvider.getString(R.string.main_activity_toast_patch_not_selected),
                    true,
                ))
                return@launch
            }
            romUri == null -> {
                _message.emit(ActionResult(
                    resourceProvider.getString(R.string.main_activity_toast_rom_not_selected),
                    true,
                ))
                return@launch
            }
            outputUri == null -> {
                _message.emit(ActionResult(
                    resourceProvider.getString(R.string.main_activity_toast_output_not_selected),
                    true,
                ))
                return@launch
            }
            else -> {
                try {
                    _actionIsRunning.value = true
                    applyPatch()
                    _message.emit(ActionResult(
                        resourceProvider.getString(R.string.notify_patching_complete),
                        false,
                    ))
                } catch (e: Exception) {
                    val errorMsg = if (e.isNoSpaceLeft()) {
                        resourceProvider.getString(R.string.notify_error_not_enough_space)
                    } else {
                        e.message ?: resourceProvider.getString(R.string.notify_error_unknown)
                    }
                    _message.emit(ActionResult(errorMsg, true))
                } finally {
                    _actionIsRunning.value = false
                    _status.value = null
                }
            }
        }
    }

    private fun checkArchive(fileName: String) {
        val isArchive = File(fileName).isArchive()
        Timber.d("isArchive = $isArchive")
        if (isArchive) {
            _message.tryEmit(ActionResult(
                resourceProvider.getString(R.string.main_activity_toast_archives_not_supported),
                true,
            ))
        }
    }

    private suspend fun suggestOutputName(romName: String) = withContext(Dispatchers.Default) {
        val baseName = fileUtils.getBaseName(romName)
        val ext = fileUtils.getExtension(romName)
        _suggestedOutputName.value = "$baseName [patched].$ext"
    }

    private suspend fun applyPatch() = withContext(Dispatchers.IO) {
        val romUri = romUri
        val patchUri = patchUri
        val outputUri = outputUri
        val patchName = _patchName.value
        require(romUri != null) { "romUri is null" }
        require(patchUri != null) { "patchUri is null" }
        require(outputUri != null) { "outputUri is null" }
        require(patchName.isNotEmpty()) { "patchName is empty" }
        var patchFile: File? = null
        var romFile: File? = null
        var outputFile: File? = null
        try {
            fileUtils.checkSpaceForPatching(
                fileUtils.getFileSize(romUri) ?: 0L,
                fileUtils.getFileSize(patchUri) ?: 0L,
            )
            _status.value = R.string.status_copying_files
            romFile = fileUtils.copyToTempFile(romUri)
            patchFile = fileUtils.copyToTempFile(patchUri, patchName)
            outputFile = File.createTempFile("output", ".rom", fileUtils.getTempDir())
            _status.value = R.string.status_applying_patch
            val patcher = patcherFactory.createPatcher(patchFile, romFile, outputFile)
            patcher.apply(settings.getIgnoreChecksum())
            fileUtils.delete(romFile)
            romFile = null
            fileUtils.delete(patchFile)
            patchFile = null
            _status.value = R.string.status_writing_result
            try {
                fileUtils.copy(outputFile, outputUri)
            } catch (e: IOException) {
                fileUtils.delete(outputUri)
                throw e
            }
            settings.setPatchingSuccessful(true)
        } finally {
            fileUtils.delete(outputFile)
            fileUtils.delete(romFile)
            fileUtils.delete(patchFile)
        }
    }

    private fun Throwable.isNoSpaceLeft(): Boolean =
        when (this) {
            is ErrnoException -> this.errno == OsConstants.ENOSPC
            is IOException ->
                this.message?.contains("No space left") == true ||
                    this.message?.contains("ENOSPC") == true ||
                    this.cause?.isNoSpaceLeft() == true
            else -> false
        }
}