/*
Copyright (C) 2013, 2016, 2021 Boris Timofeev

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
import java.util.zip.CRC32

class UPS(
    patch: File,
    rom: File,
    output: File,
    resourceProvider: ResourceProvider,
    fileUtils: FileUtils,
) : Patcher(patch, rom, output, resourceProvider, fileUtils) {

    @Throws(PatchException::class, IOException::class)
    override fun apply(ignoreChecksum: Boolean) {
        if (patchFile.length() < 18) {
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
        }

        var patchStream: BufferedInputStream? = null
        var romStream: BufferedInputStream? = null
        var outputStream: BufferedOutputStream? = null
        lateinit var upsCrc: UpsCrc
        try {
            if (!checkMagic(patchFile)) {
                throw PatchException(resourceProvider.getString(R.string.notify_error_not_ups_patch))
            }

            upsCrc = readUpsCrc(patchFile, resourceProvider)
            if (upsCrc.patchFileCRC != upsCrc.realPatchCRC) {
                throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
            }

            patchStream = BufferedInputStream(FileInputStream(patchFile))
            var patchPos = 0L
            // skip magic
            for (i in 0 until 4) {
                patchStream.read()
            }
            patchPos += 4

            // decode rom and output size
            var p = decode(patchStream)
            var xSize = p.value
            patchPos += p.size
            p = decode(patchStream)
            var ySize = p.value
            patchPos += p.size

            val realRomCrc = fileUtils.checksumCRC32(romFile)

            if (romFile.length() == xSize && realRomCrc == upsCrc.inputFileCRC) {
                // xSize, ySize, inCRC, outCRC not change
            } else if (romFile.length() == ySize && realRomCrc == upsCrc.outputFileCRC) {
                // swap(xSize, ySize) and swap(inCRC, outCRC)
                val tmp = xSize
                xSize = ySize
                ySize = tmp
                upsCrc.swapInOut()
            } else {
                if (!ignoreChecksum) {
                    throw IOException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch))
                }
            }

            romStream = BufferedInputStream(FileInputStream(romFile))
            outputStream = BufferedOutputStream(FileOutputStream(outputFile))
            var outPos = 0L

            var offset = 0L
            while (patchPos < patchFile.length() - 12) {
                p = decode(patchStream)
                offset += p.value
                patchPos += p.size
                if (offset > ySize) {
                    continue
                }
                fileUtils.copy(romStream, outputStream, offset - outPos)
                outPos += offset - outPos
                for (i in offset until ySize) {
                    val x = patchStream.read()
                    patchPos++
                    offset++
                    if (x == 0x00) {
                        break // chunk terminating byte - 0x00
                    }
                    val y = if (i < xSize) romStream.read() else 0x00
                    outputStream.write(x xor y)
                    outPos++
                }
            }
            // write rom tail and trim
            fileUtils.copy(romStream, outputStream, ySize - outPos)
        } finally {
            fileUtils.closeQuietly(patchStream)
            fileUtils.closeQuietly(romStream)
            fileUtils.closeQuietly(outputStream)
        }

        if (!ignoreChecksum) {
            val realOutCrc = fileUtils.checksumCRC32(outputFile)
            if (realOutCrc != upsCrc.outputFileCRC) {
                throw PatchException(resourceProvider.getString(R.string.notify_error_wrong_checksum_after_patching))
            }
        }
    }

    // decode pointer
    @Throws(PatchException::class, IOException::class)
    private fun decode(stream: BufferedInputStream): Pair {
        var offset = 0L
        var size = 0L
        var shift = 1L
        while (true) {
            val x = stream.read()
            if (x == -1) {
                throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
            }
            size++
            offset += (x.toLong() and 0x7f) * shift
            if (x and 0x80 != 0) {
                break
            }
            shift = shift shl 7
            offset += shift
        }
        return Pair(offset, size)
    }

    class UpsCrc(
        var inputFileCRC: Long,
        var outputFileCRC: Long,
        var patchFileCRC: Long,
        var realPatchCRC: Long,
    ) {
        fun swapInOut() {
            val tmp = inputFileCRC
            inputFileCRC = outputFileCRC
            outputFileCRC = tmp
        }
    }

    class Pair(val value: Long, val size: Long)

    companion object {

        private val MAGIC_NUMBER = byteArrayOf(0x55, 0x50, 0x53, 0x31) // "UPS1"

        @Throws(IOException::class)
        fun checkMagic(f: File): Boolean {
            FileInputStream(f).use { stream ->
                val buffer = ByteArray(4)
                stream.read(buffer)
                return buffer.contentEquals(MAGIC_NUMBER)
            }
        }

        @Throws(PatchException::class, IOException::class)
        fun readUpsCrc(f: File, resourceProvider: ResourceProvider): UpsCrc {
            BufferedInputStream(FileInputStream(f)).use { stream ->
                val crc = CRC32()
                var x: Int
                for (i in 0 until (f.length() - 12)) {
                    x = stream.read()
                    if (x == -1) {
                        throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                    }
                    crc.update(x)
                }

                var inputCrc = 0L
                for (i in 0 until 4) {
                    x = stream.read()
                    if (x == -1) {
                        throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                    }
                    crc.update(x)
                    inputCrc += (x.toLong()) shl (i * 8)
                }

                var outputCrc = 0L
                for (i in 0 until 4) {
                    x = stream.read()
                    if (x == -1) {
                        throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                    }
                    crc.update(x)
                    outputCrc += (x.toLong()) shl (i * 8)
                }

                val realPatchCrc = crc.value
                val patchCrc = readLong(stream)
                if (patchCrc == -1L) {
                    throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                }
                return UpsCrc(inputCrc, outputCrc, patchCrc, realPatchCrc)
            }
        }

        @Throws(IOException::class)
        private fun readLong(stream: BufferedInputStream): Long {
            var result = 0L
            for (i in 0 until 4) {
                val x = stream.read()
                if (x == -1) {
                    return -1
                }
                result += (x.toLong()) shl (i * 8)
            }
            return result
        }
    }
}