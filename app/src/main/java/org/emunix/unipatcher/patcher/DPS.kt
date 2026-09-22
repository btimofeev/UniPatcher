/*
Copyright (C) 2014, 2016, 2021 Boris Timofeev

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

import org.emunix.unipatcher.R
import org.emunix.unipatcher.helpers.ResourceProvider
import org.emunix.unipatcher.utils.FileUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.RandomAccessFile

class DPS(
    patch: File,
    rom: File,
    output: File,
    resourceProvider: ResourceProvider,
    fileUtils: FileUtils,
) : Patcher(patch, rom, output, resourceProvider, fileUtils) {

    @Throws(PatchException::class, IOException::class)
    override fun apply(ignoreChecksum: Boolean) {
        if (patchFile.length() < MIN_SIZE_PATCH) {
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
        }

        var patchStream: BufferedInputStream? = null
        var romStream: RandomAccessFile? = null
        var outputStream: RandomAccessFile? = null

        try {
            patchStream = BufferedInputStream(FileInputStream(patchFile))

            val buffer = ByteArray(BUFFER_SIZE)

            // check version of dps patch
            patchStream.read(buffer, 0, 198)
            if (buffer[193] != 1.toByte())
                throw PatchException(resourceProvider.getString(R.string.notify_error_not_dps_patch))

            // verify rom
            if (!ignoreChecksum) {
                val romSize = getUInt(buffer, 194)
                if (romSize != romFile.length())
                    throw IOException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch))
            }

            romStream = RandomAccessFile(romFile, "r")
            outputStream = RandomAccessFile(outputFile, "rw")

            while (patchStream.read(buffer, 0, 5) != -1) {
                val mode = buffer[0].toInt()
                var offset = getUInt(buffer, 1)
                outputStream.seek(offset)

                when (mode) {
                    COPY_DATA -> {
                        patchStream.read(buffer, 0, 8)
                        offset = getUInt(buffer, 0)
                        var length = getUInt(buffer, 4)
                        romStream.seek(offset)
                        while (length > 0) {
                            val count = if (length < BUFFER_SIZE) {
                                romStream.read(buffer, 0, length.toInt())
                            } else {
                                romStream.read(buffer, 0, BUFFER_SIZE)
                            }
                            outputStream.write(buffer, 0, count)
                            length -= count
                        }
                    }
                    ENCLOSED_DATA -> {
                        patchStream.read(buffer, 0, 4)
                        var length = getUInt(buffer, 0)
                        while (length > 0) {
                            val count = if (length < BUFFER_SIZE) {
                                patchStream.read(buffer, 0, length.toInt())
                            } else {
                                patchStream.read(buffer, 0, BUFFER_SIZE)
                            }
                            outputStream.write(buffer, 0, count)
                            length -= count
                        }
                    }
                }
            }
        } finally {
            fileUtils.closeQuietly(romStream)
            fileUtils.closeQuietly(outputStream)
            fileUtils.closeQuietly(patchStream)
        }
    }

    private fun getUInt(a: ByteArray, offset: Int): Long =
        (a[offset].toLong() and 0xff) +
            ((a[offset + 1].toLong() and 0xff) shl 8) +
            ((a[offset + 2].toLong() and 0xff) shl 16) +
            ((a[offset + 3].toLong() and 0xff) shl 24)

    companion object {
        private const val MIN_SIZE_PATCH = 136
        private const val BUFFER_SIZE = 32768
        private const val COPY_DATA = 0
        private const val ENCLOSED_DATA = 1
    }
}