/*
Copyright (C) 2016, 2017, 2020, 2021 Boris Timofeev

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
package org.emunix.unipatcher.patcher

import org.emunix.unipatcher.R.string
import org.emunix.unipatcher.utils.UFileUtils
import org.emunix.unipatcher.helpers.ResourceProvider
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class XDelta(patch: File?, rom: File?, output: File?, resourceProvider: ResourceProvider?, fileUtils: UFileUtils) :
    Patcher(patch, rom, output, resourceProvider, fileUtils) {

    @Throws(PatchException::class, IOException::class)
    override fun apply(ignoreChecksum: Boolean) {
        if (checkXDelta1(patchFile)) throw PatchException(resourceProvider.getString(string.notify_error_xdelta1_unsupported))
        try {
            System.loadLibrary("xdelta3")
        } catch (e: UnsatisfiedLinkError) {
            throw PatchException(resourceProvider.getString(string.notify_error_failed_load_lib_xdelta3))
        }
        val ret = xdelta3apply(patchFile.path, romFile.path, outputFile.path, ignoreChecksum)
        Timber.d("XDelta3 return code: %s", ret)
        when (ret) {
            NO_ERROR -> return
            ERR_UNABLE_OPEN_PATCH -> throw PatchException(
                resourceProvider.getString(string.notify_error_unable_open_file)
                        + " " + patchFile.name
            )
            ERR_UNABLE_OPEN_ROM -> throw PatchException(
                resourceProvider.getString(string.notify_error_unable_open_file)
                        + " " + romFile.name
            )
            ERR_UNABLE_OPEN_OUTPUT -> throw PatchException(
                resourceProvider.getString(string.notify_error_unable_open_file)
                        + " " + outputFile.name
            )
            ERR_WRONG_CHECKSUM -> throw PatchException(resourceProvider.getString(string.notify_error_rom_not_compatible_with_patch))
            ERR_INTERNAL -> throw PatchException(resourceProvider.getString(string.notify_error_xdelta3_internal_error))
            ERR_INVALID_INPUT -> throw PatchException(resourceProvider.getString(string.notify_error_not_xdelta3_patch))
            else -> throw PatchException(resourceProvider.getString(string.notify_error_unknown))
        }
    }

    @Throws(IOException::class)
    fun checkXDelta1(file: File?): Boolean {
        FileInputStream(file).use { stream ->
            val magic = ByteArray(8)
            stream.read(magic)
            for (xdelta1 in MAGIC_XDELTA1) {
                if (magic.contentEquals(xdelta1.toByteArray())) return true
            }
        }
        return false
    }

    private external fun xdelta3apply(
        patchPath: String?,
        romPath: String?,
        outputPath: String?,
        ignoreChecksum: Boolean
    ): Int

    companion object {

        private val MAGIC_XDELTA1 = arrayOf(
            "%XDELTA%", "%XDZ000%", "%XDZ001%",
            "%XDZ002%", "%XDZ003%", "%XDZ004%"
        )

        private const val NO_ERROR = 0
        private const val ERR_UNABLE_OPEN_PATCH = -5001
        private const val ERR_UNABLE_OPEN_ROM = -5002
        private const val ERR_UNABLE_OPEN_OUTPUT = -5003
        private const val ERR_WRONG_CHECKSUM = -5010
        private const val ERR_INTERNAL = -17710
        private const val ERR_INVALID_INPUT = -17712
    }
}