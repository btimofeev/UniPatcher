/*
 Copyright (c) 2021 Boris Timofeev

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

package org.emunix.unipatcher.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Job
import timber.log.Timber
import java.io.File
import kotlin.reflect.KFunction1

fun Fragment.registerActivityResult(
    viewModelUri: KFunction1<Uri, Job>
): ActivityResultLauncher<Intent> {
    return this.registerForActivityResult(StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.let { uri ->
                Timber.d("$uri")
                uri.data?.let(viewModelUri)
            }
        }
    }
}

fun ByteArray.bytesToHexString(): String {
    this.let { array ->
        return buildString {
            array.forEach { byte ->
                append("%x".format(byte))
            }
        }
    }
}

fun File.isArchive(): Boolean {
    val fileExt = this.extension.lowercase()
    val archives = listOf("zip", "rar", "7z", "gz", "tgz", "xz", "bz2", "tar")
    archives.forEach { archiveExt ->
        if (fileExt == archiveExt)
            return true
    }
    return false
}