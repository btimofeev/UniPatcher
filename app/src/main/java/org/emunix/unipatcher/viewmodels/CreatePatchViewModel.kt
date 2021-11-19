/*
 Copyright (c) 2017-2021 Boris Timofeev

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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.emunix.unipatcher.R
import org.emunix.unipatcher.Settings
import org.emunix.unipatcher.helpers.ConsumableEvent
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
    private val sourceName: MutableLiveData<String> = MutableLiveData()
    private val modifiedName: MutableLiveData<String> = MutableLiveData()
    private val patchName: MutableLiveData<String> = MutableLiveData()
    private val message: MutableLiveData<ConsumableEvent<String>> = MutableLiveData()
    private val actionIsRunning: MutableLiveData<Boolean> = MutableLiveData()

    fun getSourceName(): LiveData<String> = sourceName
    fun getModifiedName(): LiveData<String> = modifiedName
    fun getPatchName(): LiveData<String> = patchName
    fun getMessage(): LiveData<ConsumableEvent<String>> = message
    fun getActionIsRunning(): LiveData<Boolean> = actionIsRunning

    init {
        actionIsRunning.value = false
    }

    fun sourceSelected(uri: Uri) = viewModelScope.launch {
        sourceUri = uri
        sourceName.value = fileUtils.getFileName(uri)
    }

    fun modifiedSelected(uri: Uri) = viewModelScope.launch {
        modifiedUri = uri
        modifiedName.value = fileUtils.getFileName(uri)
    }

    fun patchSelected(uri: Uri) = viewModelScope.launch {
        patchUri = uri
        patchName.value = fileUtils.getFileName(uri)
    }

    fun runActionClicked() = viewModelScope.launch {
        when {
            sourceUri == null -> {
                message.value =
                    ConsumableEvent(resourceProvider.getString(R.string.create_patch_fragment_toast_source_not_selected))
                return@launch
            }
            modifiedUri == null -> {
                message.value =
                    ConsumableEvent(resourceProvider.getString(R.string.create_patch_fragment_toast_modified_not_selected))
                return@launch
            }
            patchUri == null -> {
                message.value =
                    ConsumableEvent(resourceProvider.getString(R.string.create_patch_fragment_toast_patch_not_selected))
                return@launch
            }
            else -> {
                try {
                    actionIsRunning.value = true
                    createPatch()
                    message.postValue(ConsumableEvent(resourceProvider.getString(R.string.notify_create_patch_complete)))
                } catch (e: Exception) {
                    val errorMsg =
                        "${resourceProvider.getString(R.string.notify_error)}: ${
                            e.message ?: resourceProvider.getString(
                                R.string.notify_error_unknown
                            )
                        }"
                    message.postValue(ConsumableEvent(errorMsg))
                } finally {
                    actionIsRunning.value = false
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

        val sourceFile = fileUtils.copyToTempFile(sourceUri)
        val modifiedFile = fileUtils.copyToTempFile(modifiedUri)
        val patchFile = File.createTempFile("patch", ".xdelta", fileUtils.getTempDir())
        try {
            val patchMaker = CreateXDelta3(patchFile, sourceFile, modifiedFile, resourceProvider)
            patchMaker.create()
            fileUtils.copy(patchFile, patchUri)
            settings.setPatchingSuccessful(true)
        } finally {
            fileUtils.delete(sourceFile)
            fileUtils.delete(modifiedFile)
            fileUtils.delete(patchFile)
        }
    }
}