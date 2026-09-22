/*
Copyright (C) 2017, 2021 Boris Timofeev

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
import org.emunix.unipatcher.utils.Crc16
import org.emunix.unipatcher.utils.FileUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile

class APS_GBA(
    patch: File,
    rom: File,
    output: File,
    resourceProvider: ResourceProvider,
    fileUtils: FileUtils,
) : Patcher(patch, rom, output, resourceProvider, fileUtils) {

    @Throws(PatchException::class, IOException::class)
    override fun apply(ignoreChecksum: Boolean) {
        var fileSize1: Long
        var fileSize2: Long
        var isOriginal = false
        var isModified = false

        val romBuf = ByteArray(CHUNK_SIZE)
        val patchBuf = ByteArray(CHUNK_SIZE)

        var patchStream: BufferedInputStream? = null
        var output: RandomAccessFile? = null

        fileUtils.copyFile(romFile, outputFile)

        try {
            patchStream = BufferedInputStream(FileInputStream(patchFile))
            output = RandomAccessFile(outputFile, "rw")

            val magic = ByteArray(4)
            var count = patchStream.read(magic)
            if (count < 4 || !magic.contentEquals(MAGIC_NUMBER))
                throw PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch))

            fileSize1 = readLEInt(patchStream)
            fileSize2 = readLEInt(patchStream)
            if (fileSize1 < 0 || fileSize2 < 0)
                throw PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch))

            var bytesLeft = patchFile.length() - 12

            while (bytesLeft > 0) {
                val offset = readLEInt(patchStream)
                val patchCrc1 = readLEChar(patchStream)
                val patchCrc2 = readLEChar(patchStream)
                bytesLeft -= 8
                if (offset < 0 || patchCrc1 < 0 || patchCrc2 < 0)
                    throw PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch))

                output.seek(offset)
                val oCount = output.read(romBuf)
                count = patchStream.read(patchBuf)
                bytesLeft -= CHUNK_SIZE
                if (count < CHUNK_SIZE)
                    throw PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch))

                if (oCount < CHUNK_SIZE) {
                    val start = if (oCount < 0) 0 else oCount
                    for (i in start until CHUNK_SIZE)
                        romBuf[i] = 0
                }

                val crc = Crc16().calculate(romBuf)

                for (i in 0 until CHUNK_SIZE)
                    romBuf[i] = (romBuf[i].toInt() xor patchBuf[i].toInt()).toByte()

                if (crc == patchCrc1) {
                    isOriginal = true
                } else if (crc == patchCrc2) {
                    isModified = true
                } else {
                    if (!ignoreChecksum)
                        throw PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch))
                }
                if (isOriginal && isModified)
                    throw PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch))

                output.seek(offset)
                output.write(romBuf)
            }
        } finally {
            fileUtils.closeQuietly(patchStream)
            fileUtils.closeQuietly(output)
        }

        if (isOriginal) {
            fileUtils.truncateFile(outputFile, fileSize2)
        } else if (isModified) {
            fileUtils.truncateFile(outputFile, fileSize1)
        }
    }

    @Throws(IOException::class)
    private fun readLEInt(stream: InputStream): Long {
        var result = 0L
        for (i in 0 until 4) {
            val x = stream.read()
            if (x == -1)
                return -1
            result += (x.toLong()) shl (i * 8)
        }
        return result
    }

    @Throws(IOException::class)
    private fun readLEChar(stream: InputStream): Int {
        var result = 0
        for (i in 0 until 2) {
            val x = stream.read()
            if (x == -1)
                return -1
            result += x shl (i * 8)
        }
        return result
    }

    companion object {
        private val MAGIC_NUMBER = byteArrayOf(0x41, 0x50, 0x53, 0x31) // APS1
        private const val CHUNK_SIZE = 65536
    }
}