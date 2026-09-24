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
import org.emunix.unipatcher.tools.SnesSmcHeader
import org.emunix.unipatcher.utils.FileUtils
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SnesSmcHeaderViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val fileUtils: FileUtils,
) : ViewModel() {

    private var romUri: Uri? = null
    private var outputUri: Uri? = null
    private var headerUri: Uri? = null
    private var headerOutputUri: Uri? = null

    private val _romName: MutableStateFlow<String> = MutableStateFlow("")
    val romName: StateFlow<String> = _romName.asStateFlow()

    private val _outputName: MutableStateFlow<String> = MutableStateFlow("")
    val outputName: StateFlow<String> = _outputName.asStateFlow()

    private val _headerName: MutableStateFlow<String> = MutableStateFlow("")
    val headerName: StateFlow<String> = _headerName.asStateFlow()

    private val _headerOutputName: MutableStateFlow<String> = MutableStateFlow("")
    val headerOutputName: StateFlow<String> = _headerOutputName.asStateFlow()

    private val _hasSmcHeader: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val hasSmcHeader: StateFlow<Boolean?> = _hasSmcHeader.asStateFlow()

    private val _suggestedOutputName: MutableStateFlow<String> = MutableStateFlow("")
    val suggestedOutputName: StateFlow<String> = _suggestedOutputName.asStateFlow()

    private val _suggestedHeaderOutputName: MutableStateFlow<String> = MutableStateFlow("")
    val suggestedHeaderOutputName: StateFlow<String> = _suggestedHeaderOutputName.asStateFlow()

    private val _infoText: MutableStateFlow<String> = MutableStateFlow("")
    val infoText: StateFlow<String> = _infoText.asStateFlow()

    private val _actionIsRunning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val actionIsRunning: StateFlow<Boolean> = _actionIsRunning.asStateFlow()

    private val _status: MutableStateFlow<Int?> = MutableStateFlow(null)
    val status: StateFlow<Int?> = _status.asStateFlow()

    private val _message: MutableSharedFlow<ActionResult> = MutableSharedFlow(extraBufferCapacity = 1)
    val message: SharedFlow<ActionResult> = _message.asSharedFlow()

    fun romSelected(uri: Uri) = viewModelScope.launch {
        romUri = uri
        outputUri = null
        headerUri = null
        headerOutputUri = null
        _outputName.value = ""
        _headerName.value = ""
        _headerOutputName.value = ""
        _romName.value = fileUtils.getFileName(uri)
        checkSmc(uri)
        suggestOutputName(_romName.value)
    }

    fun outputSelected(uri: Uri) = viewModelScope.launch {
        outputUri = uri
        _outputName.value = fileUtils.getFileName(uri)
    }

    fun headerFileSelected(uri: Uri) = viewModelScope.launch {
        headerUri = uri
        _headerName.value = fileUtils.getFileName(uri)
    }

    fun headerOutputSelected(uri: Uri) = viewModelScope.launch {
        headerOutputUri = uri
        _headerOutputName.value = fileUtils.getFileName(uri)
    }

    private suspend fun suggestOutputName(romName: String) = withContext(Dispatchers.Default) {
        val baseName = fileUtils.getBaseName(romName)
        val ext = fileUtils.getExtension(romName)
        val suffix = if (_hasSmcHeader.value == false) "[headered]" else "[headerless]"
        _suggestedOutputName.value = "$baseName $suffix.$ext"
        _suggestedHeaderOutputName.value = "$baseName.smc_header"
    }

    private suspend fun checkSmc(uri: Uri) = withContext(Dispatchers.Default) {
        val uriFileSize = fileUtils.getFileSize(uri)
        if (uriFileSize == null || uriFileSize == 0L) {
            _hasSmcHeader.value = null
            _infoText.value =
                resourceProvider.getString(R.string.snes_smc_error_unable_to_get_file_size)
            return@withContext
        }
        val checker = SnesSmcHeader()
        if (checker.isRomHasSmcHeader(uriFileSize)) {
            _hasSmcHeader.value = true
            _infoText.value = resourceProvider.getString(R.string.snes_smc_header_will_be_removed)
        } else {
            _hasSmcHeader.value = false
            _infoText.value = resourceProvider.getString(R.string.snes_smc_header_will_be_added)
        }
    }

    fun runActionClicked() = viewModelScope.launch {
        if (_actionIsRunning.value) return@launch
        when {
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
                    if (_hasSmcHeader.value == false) {
                        addSmc()
                        _message.emit(ActionResult(
                            resourceProvider.getString(R.string.notify_snes_add_smc_header_complete),
                            false,
                        ))
                    } else {
                        removeSmc()
                        _message.emit(ActionResult(
                            resourceProvider.getString(R.string.notify_snes_delete_smc_header_complete),
                            false,
                        ))
                    }
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

    private suspend fun removeSmc() = withContext(Dispatchers.IO) {
        val romUri = romUri
        val outputUri = outputUri
        require(romUri != null) { "romUri is null" }
        require(outputUri != null) { "outputUri is null" }

        var romFile: File? = null
        var outputFile: File? = null
        var headerOutputFile: File? = null
        try {
            _status.value = R.string.status_copying_files
            romFile = fileUtils.copyToTempFile(romUri)
            outputFile = fileUtils.copyToTempFile(outputUri)
            headerOutputFile = headerOutputUri?.let { fileUtils.copyToTempFile(it) }
            _status.value = R.string.status_removing_header
            SnesSmcHeader().deleteSnesSmcHeader(romFile, outputFile, headerOutputFile, resourceProvider, fileUtils)
            _status.value = R.string.status_writing_result
            fileUtils.copy(outputFile, outputUri)
            headerOutputFile?.let { fileUtils.copy(it, headerOutputUri!!) }
        } finally {
            fileUtils.delete(headerOutputFile)
            fileUtils.delete(outputFile)
            fileUtils.delete(romFile)
        }
    }

    private suspend fun addSmc() = withContext(Dispatchers.IO) {
        val romUri = romUri
        val outputUri = outputUri
        require(romUri != null) { "romUri is null" }
        require(outputUri != null) { "outputUri is null" }

        var romFile: File? = null
        var outputFile: File? = null
        var headerFile: File? = null
        try {
            _status.value = R.string.status_copying_files
            romFile = fileUtils.copyToTempFile(romUri)
            outputFile = fileUtils.copyToTempFile(outputUri)
            headerFile = headerUri?.let { fileUtils.copyToTempFile(it) }
            _status.value = R.string.status_adding_header
            SnesSmcHeader().addSnesSmcHeader(romFile, outputFile, headerFile, resourceProvider, fileUtils)
            _status.value = R.string.status_writing_result
            fileUtils.copy(outputFile, outputUri)
        } finally {
            fileUtils.delete(headerFile)
            fileUtils.delete(outputFile)
            fileUtils.delete(romFile)
        }
    }
}