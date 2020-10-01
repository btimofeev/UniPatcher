/*
 Copyright (c) 2017-2020 Boris Timofeev

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.emunix.unipatcher.R
import org.emunix.unipatcher.Settings
import org.emunix.unipatcher.Utils
import org.emunix.unipatcher.helpers.ConsumableEvent
import org.emunix.unipatcher.tools.CreateXDelta3
import timber.log.Timber
import java.io.File

class CreatePatchViewModel(val app: Application): AndroidViewModel(app)  {
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
        val name = DocumentFile.fromSingleUri(app.applicationContext, uri)?.name ?: "Undefined name"
        Timber.d("Source name: $name")
        sourceName.value = name
    }

    fun modifiedSelected(uri: Uri) = viewModelScope.launch {
        modifiedUri = uri
        val name = DocumentFile.fromSingleUri(app.applicationContext, uri)?.name ?: "Undefined name"
        Timber.d("Modified name: $name")
        modifiedName.value = name
    }

    fun patchSelected(uri: Uri) = viewModelScope.launch {
        patchUri = uri
        val name = DocumentFile.fromSingleUri(app.applicationContext, uri)?.name ?: "Undefined name"
        Timber.d("Patch name: $name")
        patchName.value = name
    }

    fun runActionClicked() = viewModelScope.launch {
        when {
            sourceUri == null -> {
                message.value = ConsumableEvent(app.getString(R.string.create_patch_fragment_toast_source_not_selected))
                return@launch
            }
            modifiedUri == null -> {
                message.value = ConsumableEvent(app.getString(R.string.create_patch_fragment_toast_modified_not_selected))
                return@launch
            }
            patchUri == null -> {
                message.value = ConsumableEvent(app.getString(R.string.create_patch_fragment_toast_patch_not_selected))
                return@launch
            }
            else -> {
                try {
                    actionIsRunning.value = true
                    createPatch()
                    message.postValue(ConsumableEvent(app.getString(R.string.notify_create_patch_complete)))
                } catch (e: Exception) {
                    val errorMsg = "${app.getString(R.string.notify_error)}: ${e.message ?: app.getString(R.string.notify_error_unknown)}"
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

        val sourceFile = Utils.copyToTempFile(app.applicationContext, sourceUri)
        val modifiedFile = Utils.copyToTempFile(app.applicationContext, modifiedUri)
        val patchFile = File.createTempFile("patch", ".xdelta", Utils.getTempDir(app.applicationContext))
        try {
            val patchMaker = CreateXDelta3(app.applicationContext, patchFile, sourceFile, modifiedFile)
            patchMaker.create()
            Utils.copy(patchFile, patchUri, app.applicationContext)
            Settings.setPatchingSuccessful(true)
        } finally {
            FileUtils.deleteQuietly(sourceFile)
            FileUtils.deleteQuietly(modifiedFile)
            FileUtils.deleteQuietly(patchFile)
        }
    }
}