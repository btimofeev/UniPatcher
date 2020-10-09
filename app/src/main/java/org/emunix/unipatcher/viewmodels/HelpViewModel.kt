/*
 Copyright (c) 2020 Boris Timofeev

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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.emunix.unipatcher.R
import org.emunix.unipatcher.helpers.ConsumableEvent
import java.io.BufferedReader
import java.io.IOException

class HelpViewModel(val app: Application): AndroidViewModel(app) {
    private val helpText: MutableLiveData<String> = MutableLiveData()
    private val message: MutableLiveData<ConsumableEvent<String>> = MutableLiveData()

    fun getHelpText(): LiveData<String> = helpText
    fun getMessage(): LiveData<ConsumableEvent<String>> = message

    fun helpInit() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val html =  app.resources.openRawResource(R.raw.faq).bufferedReader().use(BufferedReader::readText)
            helpText.postValue(html)
        } catch (e: IOException) {
            message.postValue(ConsumableEvent(app.getString(R.string.help_activity_error_cannot_load_text)))
        }
    }
}