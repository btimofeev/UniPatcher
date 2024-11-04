/*
 Copyright (c) 2016, 2018, 2020, 2021, 2024 Boris Timofeev

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

import org.apache.commons.io.input.CountingInputStream
import org.emunix.unipatcher.R
import org.emunix.unipatcher.utils.FileUtils
import org.emunix.unipatcher.helpers.ResourceProvider
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.CRC32

class BPS(
    patch: File,
    rom: File,
    output: File,
    resourceProvider: ResourceProvider,
    fileUtils: FileUtils,
) : Patcher(patch, rom, output, resourceProvider, fileUtils) {

    @Throws(PatchException::class, IOException::class)
    override fun apply(ignoreChecksum: Boolean) {
        assertPatchIsBpsFile()
        val crc = readBpsChecksums()
        checkPatchChecksum(crc)
        checkRomChecksum(ignoreChecksum, crc)
        applyBpsPatch()
        checkOutputChecksum(ignoreChecksum, crc)
    }

    private fun applyBpsPatch() {
        CountingInputStream(
            BufferedInputStream(
                FileInputStream(patchFile)
            )
        )
            .use { patchStream ->
                fileUtils.skip(patchStream, MAGIC_NUMBER.size.toLong())
                decode(patchStream) // read source rom size field and skip it
                val outputSize = decode(patchStream)
                val metadataSize = decode(patchStream)
                fileUtils.skip(patchStream, metadataSize.toLong())

                val rom = fileUtils.readFileToByteArray(romFile)
                val output = ByteArray(outputSize)
                var outputPos = 0
                var romRelOffset = 0
                var outRelOffset = 0

                while (patchStream.count < patchFile.length() - ALL_CHECKSUMS_SIZE) {
                    var length = decode(patchStream)
                    val mode = (length and 3).toByte()
                    length = (length shr 2) + 1

                    when (mode) {
                        SOURCE_READ -> {
                            System.arraycopy(rom, outputPos, output, outputPos, length)
                            outputPos += length
                        }

                        TARGET_READ -> {
                            fileUtils.copy(patchStream, length, output, outputPos)
                            outputPos += length
                        }

                        SOURCE_COPY, TARGET_COPY -> {
                            var offset = decode(patchStream)
                            offset = (if (offset and 1 == 1) -1 else 1) * (offset shr 1)

                            if (mode == SOURCE_COPY) {
                                romRelOffset += offset
                                System.arraycopy(rom, romRelOffset, output, outputPos, length)
                                romRelOffset += length
                                outputPos += length
                            } else {
                                outRelOffset += offset
                                while (length-- > 0) {
                                    output[outputPos++] = output[outRelOffset++]
                                }
                            }
                        }
                    }
                }
                fileUtils.writeByteArrayToFile(outputFile, output)
            }
    }

    private fun assertPatchIsBpsFile() {
        if (patchFile.length() < MIN_BPS_PATCH_FILE_SIZE) {
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
        }

        if (!checkMagic(patchFile)) {
            throw PatchException(resourceProvider.getString(R.string.notify_error_not_bps_patch))
        }
    }

    private fun checkPatchChecksum(crc: BpsChecksums) {
        if (crc.patchFile != crc.realPatch) {
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
        }
    }

    private fun checkRomChecksum(
        ignoreChecksum: Boolean,
        crc: BpsChecksums
    ) {
        if (!ignoreChecksum) {
            val realRomCrc = fileUtils.checksumCRC32(romFile)
            if (realRomCrc != crc.inputFile) {
                throw PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch))
            }
        }
    }

    private fun checkOutputChecksum(
        ignoreChecksum: Boolean,
        crc: BpsChecksums
    ) {
        if (!ignoreChecksum) {
            val realOutCrc = fileUtils.checksumCRC32(outputFile)
            if (realOutCrc != crc.outputFile) {
                throw PatchException(resourceProvider.getString(R.string.notify_error_wrong_checksum_after_patching))
            }
        }
    }

    private fun assertNotNegative(value: Int) {
        if (value < 0) {
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
        }
    }

    private fun decode(stream: InputStream): Int {
        var offset = 0
        var shift = 1
        var x: Int
        while (true) {
            x = stream.read()
            offset += (x and 0x7f) * shift
            if (x and 0x80 != 0) break
            shift = shift shl 7
            offset += shift
        }
        return offset
    }

    private fun readBpsChecksums(): BpsChecksums {
        val patchArray = fileUtils.readFileToByteArray(patchFile)
        var index = patchArray.size - ALL_CHECKSUMS_SIZE
        assertNotNegative(index)
        val inputCrc = readChecksum(patchArray, index)
        index += CHECKSUM_SIZE
        val outputCrc = readChecksum(patchArray, index)
        index += CHECKSUM_SIZE
        val patchCrc = readChecksum(patchArray, index)
        return BpsChecksums(inputCrc, outputCrc, patchCrc, calculatePatchChecksum(patchArray))
    }

    private fun readChecksum(array: ByteArray, offset: Int): Long {
        var checksum: Long = 0
        var index = offset
        for (i in 0..< CHECKSUM_SIZE) {
            val x = array[index++].toInt() and 0xFF
            checksum += x.toLong() shl (i * 8)
        }
        return checksum
    }

    private fun calculatePatchChecksum(array: ByteArray): Long {
        val crc = CRC32()
        crc.update(array, 0, array.size - CHECKSUM_SIZE)
        return crc.value
    }

    private data class BpsChecksums(
        val inputFile: Long,
        val outputFile: Long,
        val patchFile: Long,
        val realPatch: Long
    )

    companion object {

        private val MAGIC_NUMBER = byteArrayOf(0x42, 0x50, 0x53, 0x31) // "BPS1"
        private const val MIN_BPS_PATCH_FILE_SIZE = 19
        private const val CHECKSUM_SIZE = 4
        private const val ALL_CHECKSUMS_SIZE = 12

        private const val SOURCE_READ: Byte = 0
        private const val TARGET_READ: Byte = 1
        private const val SOURCE_COPY: Byte = 2
        private const val TARGET_COPY: Byte = 3

        @Throws(IOException::class)
        fun checkMagic(f: File): Boolean {
            val buffer = ByteArray(MAGIC_NUMBER.size)
            FileInputStream(f).use { stream ->
                stream.read(buffer)
            }
            return buffer.contentEquals(MAGIC_NUMBER)
        }
    }
}
