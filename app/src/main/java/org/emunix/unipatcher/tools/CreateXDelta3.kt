/*
 Copyright (c) 2017, 2020 Boris Timofeev

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

package org.emunix.unipatcher.tools

import android.content.Context
import org.emunix.unipatcher.R
import org.emunix.unipatcher.patcher.PatchException
import java.io.File
import java.io.IOException

class CreateXDelta3(private val context: Context, private val patchFile: File, private val romFile: File, private val outputFile: File) {

    private external fun xdelta3create(patchPath: String, sourcePath: String, modifiedPath: String): Int

    @Throws(PatchException::class, IOException::class)
    fun create() {
        try {
            System.loadLibrary("xdelta3")
        } catch (e: UnsatisfiedLinkError) {
            throw PatchException(context.getString(R.string.notify_error_failed_load_lib_xdelta3))
        }

        when (xdelta3create(patchFile.path, romFile.path, outputFile.path)) {
            NO_ERROR -> return
            ERR_UNABLE_OPEN_PATCH -> throw PatchException(context.getString(R.string.notify_error_unable_open_file)
                    + " " + patchFile.name)
            ERR_UNABLE_OPEN_SOURCE -> throw PatchException(context.getString(R.string.notify_error_unable_open_file)
                    + " " + romFile.name)
            ERR_UNABLE_OPEN_MODIFIED -> throw PatchException(context.getString(R.string.notify_error_unable_open_file)
                    + " " + outputFile.name)
            else -> throw PatchException(context.getString(R.string.notify_error_unknown))
        }
    }

    companion object {
        private const val NO_ERROR = 0
        private const val ERR_UNABLE_OPEN_PATCH = -5001
        private const val ERR_UNABLE_OPEN_SOURCE = -5004
        private const val ERR_UNABLE_OPEN_MODIFIED = -5005
    }
}