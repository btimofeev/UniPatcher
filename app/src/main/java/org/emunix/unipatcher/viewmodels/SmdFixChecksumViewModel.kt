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
import org.emunix.unipatcher.helpers.ConsumableEvent
import org.emunix.unipatcher.helpers.ResourceProvider
import org.emunix.unipatcher.tools.SmdFixChecksum
import org.emunix.unipatcher.utils.UFileUtils
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SmdFixChecksumViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val fileUtils: UFileUtils,
) : ViewModel() {

    private var romUri: Uri? = null
    private val romName: MutableLiveData<String> = MutableLiveData()
    private val message: MutableLiveData<ConsumableEvent<String>> = MutableLiveData()
    private val actionIsRunning: MutableLiveData<Boolean> = MutableLiveData()

    fun getRomName(): LiveData<String> = romName
    fun getMessage(): LiveData<ConsumableEvent<String>> = message
    fun getActionIsRunning(): LiveData<Boolean> = actionIsRunning

    init {
        actionIsRunning.value = false
    }

    fun romSelected(uri: Uri) = viewModelScope.launch {
        romUri = uri
        romName.value = fileUtils.getFileName(uri)
    }

    fun runActionClicked() = viewModelScope.launch {
        when (romUri) {
            null -> {
                message.value =
                    ConsumableEvent(resourceProvider.getString(R.string.main_activity_toast_rom_not_selected))
                return@launch
            }
            else -> {
                try {
                    actionIsRunning.value = true
                    fixChecksum()
                    message.postValue(ConsumableEvent(resourceProvider.getString(R.string.notify_smd_fix_checksum_complete)))
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

    private suspend fun fixChecksum() = withContext(Dispatchers.IO) {
        val romUri = romUri
        require(romUri != null) { "romUri is null" }

        var tmpFile: File? = null
        try {
            tmpFile = fileUtils.copyToTempFile(romUri)
            val worker = SmdFixChecksum(tmpFile, resourceProvider, fileUtils)
            worker.fixChecksum()
            fileUtils.copy(tmpFile, romUri)
        } finally {
            fileUtils.delete(tmpFile)
        }
    }
}