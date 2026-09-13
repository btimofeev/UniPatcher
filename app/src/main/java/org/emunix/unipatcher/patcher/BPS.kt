/*
 Copyright (c) 2016, 2018, 2020, 2021, 2024, 2026 Boris Timofeev

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
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
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
        RandomAccessFile(romFile, "r").use { romRaf ->
            RandomAccessFile(outputFile, "rw").use { outputRaf ->
                CountingInputStream(
                    BufferedInputStream(
                        FileInputStream(patchFile),
                        BUFFER_SIZE
                    )
                )
                    .use { patchStream ->
                        fileUtils.skipFully(patchStream, MAGIC_NUMBER.size.toLong())
                        decode(patchStream) // read source rom size field and skip it
                        val outputSize = decode(patchStream)
                        val metadataSize = decode(patchStream)
                        fileUtils.skipFully(patchStream, metadataSize.toLong())
                        if (outputSize < 0) {
                            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                        }

                        outputRaf.setLength(outputSize.toLong())
                        val rom = romRaf.channel.map(FileChannel.MapMode.READ_ONLY, 0, romRaf.length())
                        val out = outputRaf.channel.map(FileChannel.MapMode.READ_WRITE, 0, outputSize.toLong())

                        val buffer = ByteArray(BUFFER_SIZE)
                        var outputPos = 0L
                        var romRelOffset = 0L
                        var outRelOffset = 0L

                        while (patchStream.count < patchFile.length() - ALL_CHECKSUMS_SIZE) {
                            var length = decode(patchStream)
                            val mode = (length and 3).toByte()
                            length = (length shr 2) + 1
                            if (length < 0) {
                                throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                            }
                            val size = length.toLong()

                            when (mode) {
                                SOURCE_READ -> {
                                    if (outputPos + size > rom.capacity().toLong()) {
                                        throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                                    }
                                    copyRange(rom, outputPos, out, outputPos, size, buffer)
                                    outputPos += size
                                }

                                TARGET_READ -> {
                                    copyStreamToBuf(patchStream, out, outputPos, size, buffer)
                                    outputPos += size
                                }

                                SOURCE_COPY, TARGET_COPY -> {
                                    var offset = decode(patchStream)
                                    offset = (if (offset and 1 == 1) -1 else 1) * (offset shr 1)

                                    if (mode == SOURCE_COPY) {
                                        romRelOffset += offset
                                        if (romRelOffset < 0 || romRelOffset + size > rom.capacity().toLong()) {
                                            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                                        }
                                        copyRange(rom, romRelOffset, out, outputPos, size, buffer)
                                        romRelOffset += size
                                    } else {
                                        outRelOffset += offset
                                        if (outRelOffset < 0 || outRelOffset >= outputPos) {
                                            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                                        }
                                        copyTarget(out, outRelOffset, outputPos, size, buffer)
                                        outRelOffset += size
                                    }
                                    outputPos += size
                                }
                            }
                        }

                        if (outputPos != outputSize.toLong()) {
                            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                        }
                        out.force()
                    }
            }
        }
    }

    private fun copyRange(
        from: ByteBuffer,
        fromPos: Long,
        to: ByteBuffer,
        toPos: Long,
        size: Long,
        buffer: ByteArray
    ) {
        var remaining = size
        var srcPos = fromPos
        var dstPos = toPos
        while (remaining > 0) {
            val chunk = minOf(remaining, buffer.size.toLong()).toInt()
            from.position(srcPos.toInt())
            from.get(buffer, 0, chunk)
            to.position(dstPos.toInt())
            to.put(buffer, 0, chunk)
            srcPos += chunk
            dstPos += chunk
            remaining -= chunk.toLong()
        }
    }

    private fun copyStreamToBuf(
        from: InputStream,
        to: ByteBuffer,
        toPos: Long,
        size: Long,
        buffer: ByteArray
    ) {
        var remaining = size
        var dstPos = toPos
        while (remaining > 0) {
            val chunk = minOf(remaining, buffer.size.toLong()).toInt()
            var read = 0
            while (read < chunk) {
                val n = from.read(buffer, read, chunk - read)
                if (n < 0) {
                    throw IOException("Unexpected end of patch file")
                }
                read += n
            }
            to.position(dstPos.toInt())
            to.put(buffer, 0, chunk)
            dstPos += chunk
            remaining -= chunk.toLong()
        }
    }

    /**
     * Copies [size] bytes from [fromPos] to [toPos] within the target buffer.
     *
     * When the copy region reaches further than a gap distance behind the
     * write offset, the reads start pulling bytes written moments earlier
     * (this is how BPS implements run-length encoding). Because the relative
     * distance stays constant during the whole command, every written byte is
     * a repetition of the original distance bytes preceding it:
     * target[toPos + i] = target[fromPos + (i mod distance)].
     *
     * If the distance is small we materialize that small seed and write it
     * repeatedly (avoids byte-at-a-time I/O). Otherwise the gap is larger than
     * a buffer, so a straightforward chunked copy never reads overlapping data.
     */
    private fun copyTarget(
        output: ByteBuffer,
        fromPos: Long,
        toPos: Long,
        size: Long,
        buffer: ByteArray
    ) {
        val distance = toPos - fromPos
        if (size > distance && distance <= buffer.size.toLong()) {
            val seed = ByteArray(distance.toInt())
            output.position(fromPos.toInt())
            output.get(seed)

            val block = ByteArray(buffer.size)
            for (i in block.indices) {
                block[i] = seed[i % seed.size]
            }

            var remaining = size
            var dstPos = toPos
            while (remaining > 0) {
                val chunk = minOf(remaining, block.size.toLong()).toInt()
                output.position(dstPos.toInt())
                output.put(block, 0, chunk)
                dstPos += chunk
                remaining -= chunk.toLong()
            }
        } else {
            var remaining = size
            var srcPos = fromPos
            var dstPos = toPos
            while (remaining > 0) {
                val chunk = minOf(remaining, distance, buffer.size.toLong()).toInt()
                output.position(srcPos.toInt())
                output.get(buffer, 0, chunk)
                output.position(dstPos.toInt())
                output.put(buffer, 0, chunk)
                srcPos += chunk
                dstPos += chunk
                remaining -= chunk.toLong()
            }
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

    private fun decode(stream: InputStream): Int {
        var offset = 0
        var shift = 1
        while (true) {
            val x = stream.read()
            if (x < 0) {
                throw IOException("Unexpected end of patch file")
            }
            offset += (x and 0x7f) * shift
            if (x and 0x80 != 0) break
            shift = shift shl 7
            offset += shift
        }
        return offset
    }

    private fun readBpsChecksums(): BpsChecksums {
        RandomAccessFile(patchFile, "r").use { patch ->
            patch.seek(patchFile.length() - ALL_CHECKSUMS_SIZE)
            val inputCrc = readChecksum(patch)
            val outputCrc = readChecksum(patch)
            val patchCrc = readChecksum(patch)
            return BpsChecksums(inputCrc, outputCrc, patchCrc, calculatePatchChecksum())
        }
    }

    private fun readChecksum(stream: RandomAccessFile): Long {
        var checksum = 0L
        for (i in 0 until CHECKSUM_SIZE) {
            val x = stream.read()
            if (x < 0) {
                throw IOException("Unexpected end of patch file")
            }
            checksum += (x.toLong() and 0xFF) shl (i * 8)
        }
        return checksum
    }

    private fun calculatePatchChecksum(): Long {
        val crc = CRC32()
        val buffer = ByteArray(BUFFER_SIZE)
        var remaining = patchFile.length() - CHECKSUM_SIZE
        FileInputStream(patchFile).use { stream ->
            BufferedInputStream(stream, BUFFER_SIZE).use { bufferedStream ->
                while (remaining > 0) {
                    val toRead = minOf(remaining, buffer.size.toLong()).toInt()
                    val read = bufferedStream.read(buffer, 0, toRead)
                    if (read < 0) {
                        throw IOException("Unexpected end of patch file")
                    }
                    crc.update(buffer, 0, read)
                    remaining -= read.toLong()
                }
            }
        }
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
        private const val BUFFER_SIZE = 65536

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