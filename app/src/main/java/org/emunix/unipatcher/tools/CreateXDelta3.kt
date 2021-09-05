/*
 Copyright (c) 2017, 2020, 2021 Boris Timofeev

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

import org.emunix.unipatcher.R
import org.emunix.unipatcher.helpers.ResourceProvider
import org.emunix.unipatcher.patcher.PatchException
import java.io.File
import java.io.IOException

class CreateXDelta3(
    private val patchFile: File,
    private val sourceFile: File,
    private val modifiedFile: File,
    private val resourceProvider: ResourceProvider,
) {

    private external fun xdelta3create(patchPath: String, sourcePath: String, modifiedPath: String): Int

    @Throws(PatchException::class, IOException::class)
    fun create() {
        try {
            System.loadLibrary("xdelta3")
        } catch (e: UnsatisfiedLinkError) {
            throw PatchException(resourceProvider.getString(R.string.notify_error_failed_load_lib_xdelta3))
        }

        when (val ret = xdelta3create(patchFile.path, sourceFile.path, modifiedFile.path)) {
            NO_ERROR ->
                return
            ERR_UNABLE_OPEN_PATCH -> {
                throw PatchException(resourceProvider.getString(R.string.notify_error_unable_open_patch_file))
            }
            ERR_UNABLE_OPEN_SOURCE -> {
                throw PatchException(resourceProvider.getString(R.string.notify_error_unable_open_source_file))
            }
            ERR_UNABLE_OPEN_MODIFIED -> {
                throw PatchException(resourceProvider.getString(R.string.notify_error_unable_open_modified_file))
            }
            else ->
                throw PatchException("${resourceProvider.getString(R.string.notify_error_unknown)}: $ret")
        }
    }

    companion object {
        private const val NO_ERROR = 0
        private const val ERR_UNABLE_OPEN_PATCH = -5001
        private const val ERR_UNABLE_OPEN_SOURCE = -5004
        private const val ERR_UNABLE_OPEN_MODIFIED = -5005
    }
}