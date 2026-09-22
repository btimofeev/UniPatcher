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
import org.emunix.unipatcher.utils.FileUtils
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile

class APS_N64(
    patch: File,
    rom: File,
    output: File,
    resourceProvider: ResourceProvider,
    fileUtils: FileUtils,
) : Patcher(patch, rom, output, resourceProvider, fileUtils) {

    @Throws(PatchException::class, IOException::class)
    override fun apply(ignoreChecksum: Boolean) {
        var romStream: BufferedInputStream? = null
        var patchStream: BufferedInputStream? = null
        var outputStream: BufferedOutputStream? = null

        try {
            patchStream = BufferedInputStream(FileInputStream(patchFile))

            val patchSize = patchFile.length()
            val romSize = romFile.length()
            var outSize = 0L
            var romPos = 0L
            var outPos = 0L
            var patchPos = 0L
            var offset = 0L
            var size = 0L

            // check magic string
            val magic = ByteArray(5)
            var count = patchStream.read(magic)
            if (count != 5 || !magic.contentEquals(MAGIC_NUMBER))
                throw PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch))
            patchPos += 5

            // read and check type of the patch
            var patchType = patchStream.read()
            if (patchType != TYPE_SIMPLE_PATCH && patchType != TYPE_N64_PATCH)
                throw PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch))
            patchPos++

            // check encoding method
            val encoding = patchStream.read()
            if (encoding != ENCODING_SIMPLE)
                throw PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch))
            patchPos++

            // skip description
            val description = ByteArray(50)
            count = patchStream.read(description)
            if (count < 50)
                throw PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch))
            patchPos += 50

            // validate ROM
            if (patchType == TYPE_N64_PATCH) {
                val endianness = patchStream.read()
                val cardID = (((patchStream.read() and 0xff) shl 8) + (patchStream.read() and 0xff))
                val country = patchStream.read()
                val crc = ByteArray(8)
                patchStream.read(crc)
                if (!ignoreChecksum) {
                    if (!validateROM(endianness, cardID, country, crc))
                        throw PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch))
                }
                // skip bytes for future expansion
                val skip = ByteArray(5)
                patchStream.read(skip)
                patchPos += 17
            }

            // read size of destination image.
            outSize = readLELong(patchStream).toLong()
            patchPos += 4

            romStream = BufferedInputStream(FileInputStream(romFile))
            outputStream = BufferedOutputStream(FileOutputStream(outputFile))

            // apply patch
            while (patchPos < patchSize) {
                offset = readLELong(patchStream).toLong()
                if (offset < 0)
                    throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                patchPos += 4

                // copy data from rom to out
                if (offset <= romSize) {
                    if (outPos < offset) {
                        size = offset - outPos
                        fileUtils.copy(romStream, outputStream, size)
                        romPos += size
                        outPos += size
                    }
                } else {
                    if (outPos < romSize) {
                        size = romSize - outPos
                        fileUtils.copy(romStream, outputStream, size)
                        romPos += size
                        outPos += size
                    }
                    if (outPos < offset) {
                        size = offset - outPos
                        fileUtils.copy(size, 0x0.toByte(), outputStream)
                        outPos += size
                    }
                }

                // copy data from patch to out
                size = patchStream.read().toLong()
                patchPos++
                if (size != 0L) {
                    val data = ByteArray(size.toInt())
                    patchStream.read(data)
                    patchPos += size
                    outputStream.write(data)
                    outPos += size
                } else { // RLE
                    val value = patchStream.read().toByte()
                    size = patchStream.read().toLong()
                    patchPos += 2
                    val data = ByteArray(size.toInt())
                    data.fill(value)
                    outputStream.write(data)
                    outPos += size
                }

                // skip rom data
                if (offset <= romSize) {
                    if (romPos + size > romSize) {
                        romPos = romSize
                    } else {
                        val buf = ByteArray(size.toInt())
                        romStream.read(buf)
                        romPos += size
                    }
                }
            }
            // write rom tail and trim
            fileUtils.copy(romStream, outputStream, outSize - outPos)
        } finally {
            fileUtils.closeQuietly(romStream)
            fileUtils.closeQuietly(patchStream)
            fileUtils.closeQuietly(outputStream)
        }
    }

    @Throws(IOException::class)
    private fun validateROM(endianness: Int, cartID: Int, country: Int, crc: ByteArray): Boolean {
        val rom = RandomAccessFile(romFile, "r")
        try {
            // check endianness
            var value = rom.read()
            if ((endianness == 1 && value != 0x80) || (endianness == 0 && value != 0x37))
                return false

            // check cartID
            rom.seek(0x3c)
            value = if (endianness == 1) {
                ((rom.read() and 0xff) shl 8) + (rom.read() and 0xff)
            } else {
                (rom.read() and 0xff) + ((rom.read() and 0xff) shl 8)
            }
            if (cartID != value)
                return false

            // check country
            value = rom.read()
            if (endianness == 0)
                value = rom.read()
            if (country != value)
                return false

            // check crc
            val buf = ByteArray(8)
            rom.seek(0x10)
            rom.read(buf)
            if (endianness == 0) {
                var i = 0
                while (i < buf.size) {
                    val tmp = buf[i]
                    buf[i] = buf[i + 1]
                    buf[i + 1] = tmp
                    i += 2
                }
            }
            if (!crc.contentEquals(buf))
                return false
        } finally {
            fileUtils.closeQuietly(rom)
        }
        return true
    }

    @Throws(IOException::class)
    private fun readLELong(stream: InputStream): Int =
        (stream.read() and 0xff) + ((stream.read() and 0xff) shl 8) +
            ((stream.read() and 0xff) shl 16) + ((stream.read() and 0xff) shl 24)

    companion object {
        private val MAGIC_NUMBER = byteArrayOf(0x41, 0x50, 0x53, 0x31, 0x30) // APS10
        private const val TYPE_SIMPLE_PATCH = 0
        private const val TYPE_N64_PATCH = 1
        private const val ENCODING_SIMPLE = 0
    }
}