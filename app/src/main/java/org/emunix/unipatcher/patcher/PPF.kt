/*
Copyright (C) 2013, 2021 Boris Timofeev

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
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.RandomAccessFile

class PPF(
    patch: File,
    rom: File,
    output: File,
    resourceProvider: ResourceProvider,
    fileUtils: FileUtils,
) : Patcher(patch, rom, output, resourceProvider, fileUtils) {

    /**
     * Check what PPF version we have.
     *
     * @param file PPF patch
     * @return PPF patch version or 0 if the file is not a PPF patch
     */
    @Throws(IOException::class)
    private fun getPPFVersion(file: File): Int {
        var version = 0
        val stream = FileInputStream(file)
        try {
            val buffer = ByteArray(3)
            stream.read(buffer)
            if (buffer.contentEquals(MAGIC_NUMBER)) {
                val b = stream.read()
                if (b == 0x31) version = 1
                else if (b == 0x32) version = 2
                else if (b == 0x33) version = 3
            }
        } finally {
            fileUtils.closeQuietly(stream)
        }
        return version
    }

    @Throws(PatchException::class, IOException::class)
    override fun apply(ignoreChecksum: Boolean) {
        if (patchFile.length() < 61) {
            throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
        }

        fileUtils.copyFile(romFile, outputFile)

        when (getPPFVersion(patchFile)) {
            1 -> applyPPF1()
            2 -> applyPPF2(ignoreChecksum)
            3 -> applyPPF3(ignoreChecksum)
            else -> throw PatchException(resourceProvider.getString(R.string.notify_error_not_ppf_patch))
        }
    }

    @Throws(IOException::class)
    private fun applyPPF1() {
        var patchStream: RandomAccessFile? = null
        var outputStream: RandomAccessFile? = null
        try {
            patchStream = RandomAccessFile(patchFile, "r")
            outputStream = RandomAccessFile(outputFile, "rw")

            val dataEnd = patchFile.length()
            val chunkData = ByteArray(256)

            patchStream.seek(56)
            while (patchStream.filePointer < dataEnd) {
                val offset = readLittleEndianInt(patchStream)
                val chunkSize = patchStream.readUnsignedByte()
                patchStream.read(chunkData, 0, chunkSize)
                outputStream.seek(offset.toLong())
                outputStream.write(chunkData, 0, chunkSize)
            }
        } finally {
            fileUtils.closeQuietly(patchStream)
            fileUtils.closeQuietly(outputStream)
        }
    }

    @Throws(IOException::class, PatchException::class)
    private fun applyPPF2(ignoreChecksum: Boolean) {
        var patchStream: RandomAccessFile? = null
        var outputStream: RandomAccessFile? = null
        try {
            patchStream = RandomAccessFile(patchFile, "r")

            // Check size of ROM
            patchStream.seek(56)
            val romSize = readLittleEndianInt(patchStream)
            if (!ignoreChecksum) {
                if (romSize.toLong() != romFile.length()) {
                    throw PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch))
                }
            }

            outputStream = RandomAccessFile(outputFile, "rw")

            // Check binary block
            val patchBinaryBlock = ByteArray(1024)
            val romBinaryBlock = ByteArray(1024)
            outputStream.seek(0x9320)
            patchStream.read(patchBinaryBlock, 0, 1024)
            outputStream.read(romBinaryBlock, 0, 1024)
            if (!ignoreChecksum) {
                if (!patchBinaryBlock.contentEquals(romBinaryBlock))
                    throw PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch))
            }

            // Calculate end of patch data
            var dataEnd = patchFile.length()
            val sizeFileId = getSizeFileId(patchStream, 2)
            if (sizeFileId > 0) {
                dataEnd -= (18 + sizeFileId + 16 + 4)
            }

            // Apply patch
            val chunkData = ByteArray(256)

            patchStream.seek(1084)
            while (patchStream.filePointer < dataEnd) {
                val offset = readLittleEndianInt(patchStream)
                val chunkSize = patchStream.readUnsignedByte()
                patchStream.read(chunkData, 0, chunkSize)
                outputStream.seek(offset.toLong())
                outputStream.write(chunkData, 0, chunkSize)
            }
        } finally {
            fileUtils.closeQuietly(patchStream)
            fileUtils.closeQuietly(outputStream)
        }
    }

    @Throws(IOException::class, PatchException::class)
    private fun applyPPF3(ignoreChecksum: Boolean) {
        var patchStream: RandomAccessFile? = null
        var outputStream: RandomAccessFile? = null
        try {
            patchStream = RandomAccessFile(patchFile, "r")
            outputStream = RandomAccessFile(outputFile, "rw")

            patchStream.seek(56)
            val imagetype = patchStream.readByte()
            val blockcheck = patchStream.readByte()
            val undo = patchStream.readByte()

            // Check binary block
            if (blockcheck == 0x01.toByte()) {
                val patchBinaryBlock = ByteArray(1024)
                val romBinaryBlock = ByteArray(1024)
                patchStream.seek(60)
                if (imagetype == 0x01.toByte()) {
                    outputStream.seek(0x80A0)
                } else {
                    outputStream.seek(0x9320)
                }
                patchStream.read(patchBinaryBlock, 0, 1024)
                outputStream.read(romBinaryBlock, 0, 1024)
                if (!ignoreChecksum) {
                    if (!patchBinaryBlock.contentEquals(romBinaryBlock))
                        throw PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch))
                }
            }

            // Calculate end of patch data
            var dataEnd = patchFile.length()
            val sizeFileId = getSizeFileId(patchStream, 3)
            if (sizeFileId > 0) {
                dataEnd -= (18 + sizeFileId + 16 + 2)
            }

            // Seek start address of patch data
            if (blockcheck == 0x01.toByte()) {
                patchStream.seek(1084)
            } else {
                patchStream.seek(60)
            }

            // Apply patch
            val chunkData = ByteArray(512)

            while (patchStream.filePointer < dataEnd) {
                val offset = readLittleEndianLong(patchStream)
                val chunkSize = patchStream.readUnsignedByte()
                patchStream.read(chunkData, 0, chunkSize)
                if (undo == 0x01.toByte()) patchStream.seek(patchStream.filePointer + chunkSize)
                outputStream.seek(offset)
                outputStream.write(chunkData, 0, chunkSize)
            }
        } finally {
            fileUtils.closeQuietly(patchStream)
            fileUtils.closeQuietly(outputStream)
        }
    }

    @Throws(IOException::class)
    private fun readLittleEndianLong(stream: RandomAccessFile): Long {
        val b = ByteArray(8)
        stream.read(b)
        return ((b[7].toLong() and 0xff) shl 56) + ((b[6].toLong() and 0xff) shl 48) +
            ((b[5].toLong() and 0xff) shl 40) + ((b[4].toLong() and 0xff) shl 32) +
            ((b[3].toLong() and 0xff) shl 24) + ((b[2].toLong() and 0xff) shl 16) +
            ((b[1].toLong() and 0xff) shl 8) + (b[0].toLong() and 0xff)
    }

    @Throws(IOException::class)
    private fun readLittleEndianInt(stream: RandomAccessFile): Int {
        val b = ByteArray(4)
        stream.read(b)
        return ((b[3].toInt() and 0xff) shl 24) + ((b[2].toInt() and 0xff) shl 16) +
            ((b[1].toInt() and 0xff) shl 8) + (b[0].toInt() and 0xff)
    }

    /**
     * Returns size of FileID
     *
     * @param stream     stream of PPF file
     * @param ppfVersion version of PPF patch
     * @return size of FileID or 0
     */
    @Throws(IOException::class)
    private fun getSizeFileId(stream: RandomAccessFile, ppfVersion: Int): Int {
        val magic = byteArrayOf(0x2E, 0x44, 0x49, 0x5A) // ".DIZ"
        val buffer = ByteArray(4)

        if (ppfVersion == 2) {
            stream.seek(stream.length() - 4 - 4)
        } else {
            stream.seek(stream.length() - 2 - 4)
        }

        stream.read(buffer, 0, 4)
        if (!buffer.contentEquals(magic)) {
            return 0
        }

        var result: Int
        if (ppfVersion == 2) {
            result = readLittleEndianInt(stream)
        } else {
            result = stream.readUnsignedByte() + (stream.readUnsignedByte() shl 8)
        }

        if (result > 3072) result = 3072
        return result
    }

    companion object {
        private val MAGIC_NUMBER = byteArrayOf(0x50, 0x50, 0x46) // "PPF" without version
    }
}