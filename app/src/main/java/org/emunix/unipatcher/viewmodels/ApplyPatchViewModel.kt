/*
 Copyright (c) 2013-2021 Boris Timofeev

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

import android.app.Application
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.emunix.unipatcher.R
import org.emunix.unipatcher.Settings
import org.emunix.unipatcher.Utils
import org.emunix.unipatcher.helpers.ConsumableEvent
import org.emunix.unipatcher.patcher.PatcherFactory

import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ApplyPatchViewModel @Inject constructor(val app: Application, val settings: Settings): AndroidViewModel(app) {

    private var patchUri: Uri? = null
    private var romUri: Uri? = null
    private var outputUri: Uri? = null
    private val patchName: MutableLiveData<String> = MutableLiveData()
    private val romName: MutableLiveData<String> = MutableLiveData()
    private val outputName: MutableLiveData<String> = MutableLiveData()
    private val suggestedOutputName: MutableLiveData<String> = MutableLiveData()
    private val message: MutableLiveData<ConsumableEvent<String>> = MutableLiveData()
    private val actionIsRunning: MutableLiveData<Boolean> = MutableLiveData()

    fun getPatchName(): LiveData<String> = patchName
    fun getRomName(): LiveData<String> = romName
    fun getOutputName(): LiveData<String> = outputName
    fun getSuggestedOutputName(): LiveData<String> = suggestedOutputName
    fun getMessage(): LiveData<ConsumableEvent<String>> = message
    fun getActionIsRunning(): LiveData<Boolean> = actionIsRunning

    init {
        actionIsRunning.value = false
    }

    fun patchSelected(uri: Uri) = viewModelScope.launch {
        patchUri = uri
        val name = DocumentFile.fromSingleUri(app.applicationContext, uri)?.name ?: "Undefined name"
        Timber.d("Patch name: $name")
        patchName.value = name
        checkArchive(name)
    }

    fun romSelected(uri: Uri) = viewModelScope.launch {
        romUri = uri
        val name = DocumentFile.fromSingleUri(app.applicationContext, uri)?.name ?: "Undefined name"
        Timber.d("ROM name: $name")
        romName.value = name
        checkArchive(name)
        suggestOutputName(name)
    }

    fun outputSelected(uri: Uri) = viewModelScope.launch {
        outputUri = uri
        val name = DocumentFile.fromSingleUri(app.applicationContext, uri)?.name ?: "Undefined name"
        Timber.d("Output name: $name")
        outputName.value = name
    }

    fun runActionClicked() = viewModelScope.launch {
        when {
            patchUri == null -> {
                message.value = ConsumableEvent(app.getString(R.string.main_activity_toast_patch_not_selected))
                return@launch
            }
            romUri == null -> {
                message.value = ConsumableEvent(app.getString(R.string.main_activity_toast_rom_not_selected))
                return@launch
            }
            outputUri == null -> {
                message.value = ConsumableEvent(app.getString(R.string.main_activity_toast_output_not_selected))
                return@launch
            }
            else -> {
                try {
                    actionIsRunning.value = true
                    applyPatch()
                    message.postValue(ConsumableEvent(app.getString(R.string.notify_patching_complete)))
                } catch (e: Exception) {
                    val errorMsg = "${app.getString(R.string.notify_error)}: ${e.message ?: app.getString(R.string.notify_error_unknown)}"
                    message.postValue(ConsumableEvent(errorMsg))
                } finally {
                    actionIsRunning.value = false
                }
            }
        }
    }

    private suspend fun checkArchive(fileName: String) {
        val isArchive = Utils.isArchive(fileName)
        Timber.d("isArchive = $isArchive")
        if (isArchive)
            message.value = ConsumableEvent(app.getString(R.string.main_activity_toast_archives_not_supported))
    }

    private suspend fun suggestOutputName(romName: String) = withContext(Dispatchers.Default) {
        val baseName = FilenameUtils.getBaseName(romName)
        val ext = FilenameUtils.getExtension(romName)
        suggestedOutputName.postValue("$baseName [patched].$ext")
    }

    private suspend fun applyPatch() = withContext(Dispatchers.IO) {
        val romUri = romUri
        val patchUri = patchUri
        val outputUri = outputUri
        val patchName = patchName.value
        require(romUri != null) { "romUri is null" }
        require(patchUri != null) { "patchUri is null" }
        require(outputUri != null) { "outputUri is null" }
        require(patchName != null) { "patchName is null" }
        var patchFile: File? = null
        var romFile: File? = null
        var outputFile: File? = null
        try {
            romFile = Utils.copyToTempFile(app.applicationContext, romUri)
            patchFile = Utils.copyToTempFile(app.applicationContext, patchUri, patchName)
            outputFile = File.createTempFile("output", ".rom", Utils.getTempDir(app.applicationContext))
            val patcher = PatcherFactory.createPatcher(app.applicationContext, patchFile, romFile, outputFile)
            patcher.apply(settings.getIgnoreChecksum())
            Utils.copy(outputFile, outputUri, app.applicationContext)
            settings.setPatchingSuccessful(true)
        } finally {
            FileUtils.deleteQuietly(outputFile)
            FileUtils.deleteQuietly(romFile)
            FileUtils.deleteQuietly(patchFile)
        }
    }
}