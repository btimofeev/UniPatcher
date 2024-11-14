/*
Copyright (C) 2013, 2016, 2017, 2020, 2021, 2024 Boris Timofeev

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
import org.emunix.unipatcher.patcher.IPS.PatchType.IPS32_PATCH
import org.emunix.unipatcher.patcher.IPS.PatchType.IPS_PATCH
import org.emunix.unipatcher.patcher.IPS.PatchType.NOT_IPS_PATCH
import org.emunix.unipatcher.utils.FileUtils
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class IPS(
    patch: File,
    rom: File,
    output: File,
    resourceProvider: ResourceProvider,
    fileUtils: FileUtils,
) : Patcher(patch, rom, output, resourceProvider, fileUtils) {

    private var mPatchType = NOT_IPS_PATCH

    @Throws(PatchException::class, IOException::class)
    override fun apply(ignoreChecksum: Boolean) {
        apply()
    }

    @Throws(PatchException::class, IOException::class)
    fun apply() {
        assertFileSizeNotSmall()

        var romStream: BufferedInputStream? = null
        var patchStream: BufferedInputStream? = null
        var outputStream: BufferedOutputStream? = null

        try {
            romStream = BufferedInputStream(FileInputStream(romFile))
            patchStream = BufferedInputStream(FileInputStream(patchFile))
            outputStream = BufferedOutputStream(FileOutputStream(outputFile))

            readHeader(patchStream)

            val romSize = romFile.length()
            var romPos: Long = 0
            var outPos: Long = 0

            while (true) {
                val offset = readOffset(patchStream)
                assertNotNegative(offset)
                if (checkEOF(offset)) {
                    // truncate file or copy tail
                    if (romPos < romSize) {
                        val truncateOffset = readOffset(patchStream)
                        val tailSize = if (truncateOffset != END_OF_STREAM && truncateOffset >= romPos) {
                            truncateOffset - romPos
                        } else {
                            romSize - romPos
                        }
                        fileUtils.copy(romStream, outputStream, tailSize)
                    }
                    break
                }

                if (offset <= romSize) {
                    if (outPos < offset) {
                        val size = offset - outPos
                        fileUtils.copy(romStream, outputStream, size)
                        romPos += size
                        outPos += size
                    }
                } else {
                    if (outPos < romSize) {
                        val size = romSize - outPos
                        fileUtils.copy(romStream, outputStream, size)
                        romPos += size
                        outPos += size
                    }
                    if (outPos < offset) {
                        val size = offset - outPos
                        fileUtils.copy(size, 0x0.toByte(), outputStream)
                        outPos += size
                    }
                }

                var size = ((patchStream.read() shl ONE_BYTE) + patchStream.read()).toLong()
                if (size == RLE_FLAG) {
                    size = ((patchStream.read() shl ONE_BYTE) + patchStream.read()).toLong()
                    assertNotNegative(size)
                    val value = patchStream.read().toByte()
                    fileUtils.copy(number = size, b = value, to = outputStream)
                    outPos += size
                } else {
                    assertNotNegative(size)
                    fileUtils.copy(from = patchStream, to = outputStream, size = size)
                    outPos += size
                }

                if (offset <= romSize) {
                    if (romPos + size > romSize) {
                        romPos = romSize
                    } else {
                        assertNotNegative(size)
                        val buf = ByteArray(size.toInt())
                        romStream.read(buf)
                        romPos += size
                    }
                }
            }
        } finally {
            fileUtils.closeQuietly(romStream)
            fileUtils.closeQuietly(patchStream)
            fileUtils.closeQuietly(outputStream)
        }
    }

    private fun assertNotNegative(value: Long) {
        if (value < 0) {
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
        }
    }

    private fun assertFileSizeNotSmall() {
        if (patchFile.length() < IPS_FILE_MIN_SIZE) {
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
        }
    }

    private fun readHeader(patchStream: BufferedInputStream) {
        val magic = ByteArray(MAGIC_LENGTH)
        patchStream.read(magic).toLong()
        mPatchType = when {
            magic.contentEquals(MAGIC_NUMBER_IPS) -> IPS_PATCH
            magic.contentEquals(MAGIC_NUMBER_IPS32) -> IPS32_PATCH
            else -> throw PatchException(resourceProvider.getString(R.string.notify_error_not_ips_patch))
        }
    }

    private fun checkEOF(value: Long): Boolean =
        when (mPatchType) {
            IPS_PATCH -> value == EOF
            IPS32_PATCH -> value == EEOF
            else -> false
        }

    @Throws(IOException::class)
    private fun readOffset(stream: InputStream): Long {
        var offset: Long = 0
        var numBytes = when (mPatchType) {
            IPS_PATCH -> 3
            IPS32_PATCH -> 4
            else -> throw IOException("Internal IPS error")
        }

        while (numBytes-- != 0) {
            val b = stream.read()
            if (b == -1) return END_OF_STREAM
            offset = (offset shl ONE_BYTE) + b
        }
        return offset
    }

    private enum class PatchType {
        NOT_IPS_PATCH,
        IPS_PATCH,
        IPS32_PATCH
    }

    private companion object {

        private val MAGIC_NUMBER_IPS = byteArrayOf(0x50, 0x41, 0x54, 0x43, 0x48) // "PATCH"
        private val MAGIC_NUMBER_IPS32 = byteArrayOf(0x49, 0x50, 0x53, 0x33, 0x32) // "IPS32"
        private const val MAGIC_LENGTH = 5

        private const val EOF = 0x454f46L
        private const val EEOF = 0x45454f46L

        private const val IPS_FILE_MIN_SIZE = 14

        private const val ONE_BYTE = 8
        private const val END_OF_STREAM = -1L
        private const val RLE_FLAG = 0L
    }
}
