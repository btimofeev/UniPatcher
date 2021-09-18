/*
 Copyright (c) 2016, 2018, 2020, 2021 Boris Timofeev

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

import org.apache.commons.io.FileUtils
import org.emunix.unipatcher.R
import org.emunix.unipatcher.utils.UFileUtils
import org.emunix.unipatcher.helpers.ResourceProvider
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.CRC32

class BPS(
    patch: File,
    rom: File,
    output: File,
    resourceProvider: ResourceProvider,
    fileUtils: UFileUtils,
) : Patcher(patch, rom, output, resourceProvider, fileUtils) {

    @Throws(PatchException::class, IOException::class)
    override fun apply(ignoreChecksum: Boolean) {

        if (patchFile.length() < 19) {
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
        }

        if (!checkMagic(patchFile))
            throw PatchException(resourceProvider.getString(R.string.notify_error_not_bps_patch))

        val patch = FileUtils.readFileToByteArray(patchFile)

        val crc = readBpsCrc(patch)
        if (crc.patchFile != crc.realPatch)
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))

        if (!ignoreChecksum) {
            val realRomCrc = FileUtils.checksumCRC32(romFile)
            if (realRomCrc != crc.inputFile) {
                throw PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch))
            }
        }

        var patchPos = 4
        var decoded: Pair<Int, Int>

        // decode rom size
        decoded = decode(patch, patchPos)
        patchPos = decoded.component2()
        val rom = FileUtils.readFileToByteArray(romFile)

        // decode output size
        decoded = decode(patch, patchPos)
        val outputSize = decoded.component1()
        if (outputSize > Int.MAX_VALUE)
            throw PatchException("The output file is too large.")
        if (outputSize < 0)
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
        patchPos = decoded.component2()
        val output = ByteArray(outputSize)
        var outputPos = 0

        // decode metadata size and skip
        decoded = decode(patch, patchPos)
        val metadataSize = decoded.component1()
        patchPos = decoded.component2() + metadataSize

        var romRelOffset = 0
        var outRelOffset = 0
        var offset: Int
        var length: Int
        var mode: Byte

        while (patchPos < patch.size - 12) {
            decoded = decode(patch, patchPos)
            length = decoded.component1()
            patchPos = decoded.component2()
            mode = (length and 3).toByte()
            length = (length shr 2) + 1

            when (mode) {
                SOURCE_READ -> {
                    System.arraycopy(rom, outputPos, output, outputPos, length)
                    outputPos += length
                }
                TARGET_READ -> {
                    System.arraycopy(patch, patchPos, output, outputPos, length)
                    patchPos += length
                    outputPos += length
                }
                SOURCE_COPY, TARGET_COPY -> {
                    decoded = decode(patch, patchPos)
                    offset = decoded.component1()
                    patchPos = decoded.component2()
                    offset = (if (offset and 1 == 1) -1 else 1) * (offset shr 1)

                    if (mode == SOURCE_COPY) {
                        romRelOffset += offset
                        System.arraycopy(rom, romRelOffset, output, outputPos, length)
                        romRelOffset += length
                        outputPos += length
                    } else {
                        outRelOffset += offset
                        while (length-- > 0)
                            output[outputPos++] = output[outRelOffset++]
                    }
                }
            }
        }

        FileUtils.writeByteArrayToFile(outputFile, output)

        if (!ignoreChecksum) {
            val realOutCrc = FileUtils.checksumCRC32(outputFile)
            if (realOutCrc != crc.outputFile)
                throw PatchException(resourceProvider.getString(R.string.notify_error_wrong_checksum_after_patching))
        }
    }

    private fun decode(array: ByteArray, pos: Int): Pair<Int, Int> {
        if (pos < 0)
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
        var newPos = pos
        var offset = 0
        var shift = 1
        var x: Int
        while (true) {
            x = array[newPos++].toInt()
            offset += (x and 0x7f) * shift
            if (x and 0x80 != 0) break
            shift = shift shl 7
            offset += shift
        }
        return Pair(offset, newPos)
    }

    private fun readBpsCrc(array: ByteArray): BpsCrc {
        var x: Int
        val crc = CRC32()
        crc.update(array, 0, array.size - 4)
        val realPatchCrc = crc.value

        var index = array.size - 12
        if (index < 0)
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))

        var inputCrc: Long = 0
        for (i in 0..3) {
            x = array[index++].toInt() and 0xFF
            inputCrc += x.toLong() shl (i * 8)
        }

        var outputCrc: Long = 0
        for (i in 0..3) {
            x = array[index++].toInt() and 0xFF
            outputCrc += x.toLong() shl (i * 8)
        }

        var patchCrc: Long = 0
        for (i in 0..3) {
            x = array[index++].toInt() and 0xFF
            patchCrc += x.toLong() shl (i * 8)
        }

        return BpsCrc(inputCrc, outputCrc, patchCrc, realPatchCrc)
    }

    private data class BpsCrc(val inputFile: Long, val outputFile: Long, val patchFile: Long, val realPatch: Long)

    companion object {

        private val MAGIC_NUMBER = byteArrayOf(0x42, 0x50, 0x53, 0x31) // "BPS1"
        private const val SOURCE_READ: Byte = 0
        private const val TARGET_READ: Byte = 1
        private const val SOURCE_COPY: Byte = 2
        private const val TARGET_COPY: Byte = 3

        @Throws(IOException::class)
        fun checkMagic(f: File): Boolean {
            val buffer = ByteArray(4)
            FileInputStream(f).use { it.read(buffer) }
            return buffer.contentEquals(MAGIC_NUMBER)
        }
    }
}
