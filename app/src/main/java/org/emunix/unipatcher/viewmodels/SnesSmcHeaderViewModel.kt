/*
 Copyright (c) 2014-2021 Boris Timofeev

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.emunix.unipatcher.R
import org.emunix.unipatcher.Utils
import org.emunix.unipatcher.helpers.ConsumableEvent
import org.emunix.unipatcher.helpers.ResourceProvider
import org.emunix.unipatcher.tools.SnesSmcHeader
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SnesSmcHeaderViewModel @Inject constructor(
    private val app: Application,
    private val resourceProvider: ResourceProvider,
) : AndroidViewModel(app) {

    private var romUri: Uri? = null
    private var outputUri: Uri? = null
    private val romName: MutableLiveData<String> = MutableLiveData()
    private val outputName: MutableLiveData<String> = MutableLiveData()
    private val suggestedOutputName: MutableLiveData<String> = MutableLiveData()
    private val infoText: MutableLiveData<String> = MutableLiveData()
    private val message: MutableLiveData<ConsumableEvent<String>> = MutableLiveData()
    private val actionIsRunning: MutableLiveData<Boolean> = MutableLiveData()

    fun getRomName(): LiveData<String> = romName
    fun getOutputName(): LiveData<String> = outputName
    fun getSuggestedOutputName(): LiveData<String> = suggestedOutputName
    fun getInfoText(): LiveData<String> = infoText
    fun getMessage(): LiveData<ConsumableEvent<String>> = message
    fun getActionIsRunning(): LiveData<Boolean> = actionIsRunning

    init {
        actionIsRunning.value = false
    }

    fun romSelected(uri: Uri) = viewModelScope.launch {
        romUri = uri
        val name = DocumentFile.fromSingleUri(app.applicationContext, uri)?.name ?: "Undefined name"
        Timber.d("ROM name: $name")
        romName.value = name
        checkSmc(uri)
        suggestOutputName(name)
    }

    fun outputSelected(uri: Uri) = viewModelScope.launch {
        outputUri = uri
        val name = DocumentFile.fromSingleUri(app.applicationContext, uri)?.name ?: "Undefined name"
        Timber.d("Output name: $name")
        outputName.value = name
    }

    private suspend fun suggestOutputName(romName: String) = withContext(Dispatchers.Default) {
        val baseName = FilenameUtils.getBaseName(romName)
        val ext = FilenameUtils.getExtension(romName)
        suggestedOutputName.postValue("$baseName [headerless].$ext")
    }

    private suspend fun checkSmc(uri: Uri) = withContext(Dispatchers.Default) {
        val uriFileSize = DocumentFile.fromSingleUri(app.applicationContext, uri)?.length()
        if (uriFileSize == null || uriFileSize == 0L) {
            infoText.postValue(resourceProvider.getString(R.string.snes_smc_error_unable_to_get_file_size))
            return@withContext
        }
        val checker = SnesSmcHeader()
        if (checker.isRomHasSmcHeader(uriFileSize)) {
            infoText.postValue(resourceProvider.getString(R.string.snes_smc_header_will_be_removed))
        } else {
            infoText.postValue(resourceProvider.getString(R.string.snes_rom_has_no_smc_header))
        }
    }

    fun runActionClicked() = viewModelScope.launch {
        when {
            romUri == null -> {
                message.value =
                    ConsumableEvent(resourceProvider.getString(R.string.main_activity_toast_rom_not_selected))
                return@launch
            }
            outputUri == null -> {
                message.value =
                    ConsumableEvent(resourceProvider.getString(R.string.main_activity_toast_output_not_selected))
                return@launch
            }
            else -> {
                try {
                    actionIsRunning.value = true
                    removeSmc()
                    message.postValue(ConsumableEvent(resourceProvider.getString(R.string.notify_snes_delete_smc_header_complete)))
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

    private suspend fun removeSmc() = withContext(Dispatchers.IO) {
        val romUri = romUri
        val outputUri = outputUri
        require(romUri != null) { "romUri is null" }
        require(outputUri != null) { "outputUri is null" }

        var romFile: File? = null
        var outputFile: File? = null
        try {
            romFile = Utils.copyToTempFile(app.applicationContext, romUri)
            outputFile = Utils.copyToTempFile(app.applicationContext, outputUri)
            SnesSmcHeader().deleteSnesSmcHeader(romFile, outputFile, resourceProvider)
            Utils.copy(outputFile, outputUri, app.applicationContext)
        } finally {
            FileUtils.deleteQuietly(outputFile)
            FileUtils.deleteQuietly(romFile)
        }
    }
}