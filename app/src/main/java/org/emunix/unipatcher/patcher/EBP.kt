/*
This file based on source code of EBPatcher by Marc Gagné (https://github.com/Lyrositor/EBPatcher)

Copyright (C) 2016, 2020, 2021 Boris Timofeev

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
import org.emunix.unipatcher.tools.RomException
import org.emunix.unipatcher.tools.SnesSmcHeader
import org.emunix.unipatcher.utils.FileUtils
import org.emunix.unipatcher.utils.bytesToHexString
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class EBP(
    patch: File,
    rom: File,
    output: File,
    resourceProvider: ResourceProvider,
    fileUtils: FileUtils,
) : Patcher(patch, rom, output, resourceProvider, fileUtils) {

    @Throws(PatchException::class, IOException::class)
    override fun apply(ignoreChecksum: Boolean) {
        val cleanRom = File.createTempFile("rom", null, resourceProvider.tempDir)
        val ipsPatch = File.createTempFile("patch", null, resourceProvider.tempDir)
        try {
            fileUtils.copyFile(romFile, cleanRom)
            prepareCleanRom(cleanRom, ignoreChecksum)

            EBPtoIPS(patchFile, ipsPatch)

            val ips = IPS(ipsPatch, cleanRom, outputFile, resourceProvider, fileUtils)
            ips.apply()
        } finally {
            fileUtils.delete(ipsPatch)
            fileUtils.delete(cleanRom)
        }
    }

    @Throws(IOException::class, PatchException::class)
    private fun prepareCleanRom(file: File, ignoreChecksum: Boolean) {
        // delete smc header
        try {
            SnesSmcHeader().deleteSnesSmcHeader(romFile, file, null, resourceProvider, fileUtils)
        } catch (_: RomException) {
            // no header
        }

        // check rom size and remove unused expanded space
        if (!ignoreChecksum) {
            if (file.length() < EB_CLEAN_ROM_SIZE) {
                throw PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch))
            }
        }
        if (file.length() > EB_CLEAN_ROM_SIZE && checkExpanded(file)) {
            removeExpanded(file)
        }

        // try to fix the ROM if it's incorrect
        if (!checkMD5(file)) {
            repairRom(file)
        }

        // if we couldn't fix the ROM, try to remove a 0xff byte at the end.
        if (!checkMD5(file)) {
            val length = file.length().toInt()
            val buffer = ByteArray(length)
            val input = FileInputStream(file)
            val count = input.read(buffer)
            input.close()
            if (count.toLong() != file.length()) {
                throw IOException("Unable read file")
            }
            if (buffer[length - 1] == 0xff.toByte()) {
                buffer[length - 1] = 0
            }

            if (checkMD5(buffer)) {
                val f = RandomAccessFile(file, "rw")
                f.seek((length - 1).toLong())
                f.write(0)
                f.close()
            }
        }

        if (!checkMD5(file) || !checkEarthBound(file)) {
            throw PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch))
        }
    }

    @Throws(IOException::class)
    private fun checkExpanded(file: File): Boolean {
        val byteArray = ByteArray(EB_CLEAN_ROM_SIZE)
        val f = FileInputStream(file)
        val count = f.read(byteArray)
        fileUtils.closeQuietly(f)
        if (count < EB_CLEAN_ROM_SIZE) {
            throw IOException("Unable to read 0x300000 bytes from ROM")
        }
        // ExHiROM expanded ROMs have two bytes different from LoROM.
        byteArray[0xffd5] = 0x31.toByte()
        byteArray[0xffd7] = 0x0c.toByte()

        // If the normal area is unmodified, then the expanded area is unused and can be deleted.
        return checkMD5(byteArray)
    }

    @Throws(IOException::class)
    private fun removeExpanded(file: File) {
        if (file.length() > 0x400000) {
            val f = RandomAccessFile(file, "rw")
            f.seek(0xffd5)
            f.write(0x31)
            f.seek(0xffd7)
            f.write(0x0c)
            f.close()
        }
        val fc = FileOutputStream(file, true).channel
        fc.truncate(EB_CLEAN_ROM_SIZE.toLong())
        fc.close()
    }

    @Throws(IOException::class, PatchException::class)
    private fun repairRom(file: File) {
        val md5 = calculateMD5(file)
        if (EB_WRONG_MD5.containsKey(md5)) {

            // copy patch from assets
            val input: InputStream = resourceProvider.getAsset(EB_WRONG_MD5.getValue(md5))
            val patch = File.createTempFile("patch", null, resourceProvider.tempDir)
            fileUtils.copyToFile(input, patch)
            fileUtils.closeQuietly(input)

            // fix rom
            val tmpFile = File.createTempFile("rom", null, resourceProvider.tempDir)
            fileUtils.copyFile(file, tmpFile)
            val ips = IPS(patch, tmpFile, file, resourceProvider, fileUtils)
            ips.apply()

            fileUtils.delete(tmpFile)
            fileUtils.delete(patch)
        }
    }

    private fun checkMD5(array: ByteArray): Boolean =
        try {
            val md5Digest = MessageDigest.getInstance("MD5")
            md5Digest.update(array)
            md5Digest.digest().bytesToHexString() == EB_CLEAN_MD5
        } catch (e: NoSuchAlgorithmException) {
            throw IOException(e.message)
        }

    @Throws(IOException::class)
    private fun checkMD5(file: File): Boolean = calculateMD5(file) == EB_CLEAN_MD5

    @Throws(IOException::class)
    private fun calculateMD5(file: File): String {
        val f = FileInputStream(file)
        return try {
            val md5Digest = MessageDigest.getInstance("MD5")
            val byteArray = ByteArray(32768)
            var count: Int
            while (f.read(byteArray).also { count = it } != -1) {
                md5Digest.update(byteArray, 0, count)
            }
            md5Digest.digest().bytesToHexString()
        } catch (e: NoSuchAlgorithmException) {
            throw IOException(e.message)
        } finally {
            fileUtils.closeQuietly(f)
        }
    }

    @Throws(IOException::class)
    private fun checkEarthBound(file: File): Boolean {
        val buffer = ByteArray(11)
        val f = RandomAccessFile(file, "r")
        f.seek(0xffc0)
        f.read(buffer)
        f.close()
        return buffer.contentEquals(EARTH_BOUND)
    }

    @Throws(IOException::class, PatchException::class)
    private fun EBPtoIPS(ebpFile: File, ipsFile: File) {
        var ebp: BufferedInputStream? = null
        var ips: BufferedOutputStream? = null
        try {
            ebp = BufferedInputStream(FileInputStream(ebpFile))
            ips = BufferedOutputStream(FileOutputStream(ipsFile))

            val buffer = ByteArray(65536)

            if (ebpFile.length() < 14) {
                throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
            }

            // check magic string
            val magic = ByteArray(5)
            var size = ebp.read(magic)
            if (size != 5 || !magic.contentEquals(MAGIC_NUMBER)) {
                throw PatchException(resourceProvider.getString(R.string.notify_error_not_ebp_patch))
            }

            ips.write(magic)

            while (true) {
                size = ebp.read(buffer, 0, 3)
                if (size < 3) {
                    throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                }
                ips.write(buffer, 0, 3)
                if (buffer[0] == 0x45.toByte() && buffer[1] == 0x4f.toByte() && buffer[2] == 0x46.toByte()) { // EOF
                    break
                }
                size = ebp.read(buffer, 0, 2)
                if (size < 2) {
                    throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                }
                ips.write(buffer, 0, 2)
                size = ((buffer[0].toInt() and 0xff) shl 8) + (buffer[1].toInt() and 0xff)
                if (size != 0) {
                    val count = ebp.read(buffer, 0, size)
                    if (count < size) {
                        throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                    }
                    ips.write(buffer, 0, size)
                } else {
                    size = ebp.read(buffer, 0, 3)
                    if (size < 3) {
                        throw PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                    }
                    ips.write(buffer, 0, 3)
                }
            }
        } finally {
            fileUtils.closeQuietly(ips)
            fileUtils.closeQuietly(ebp)
        }
    }

    companion object {
        private val MAGIC_NUMBER = byteArrayOf(0x50, 0x41, 0x54, 0x43, 0x48) // "PATCH"
        private val EARTH_BOUND = byteArrayOf(0x45, 0x41, 0x52, 0x54, 0x48, 0x20, 0x42, 0x4f, 0x55, 0x4e, 0x44) // "EARTH BOUND"
        private const val EB_CLEAN_MD5 = "a864b2e5c141d2dec1c4cbed75a42a85"
        private const val EB_CLEAN_ROM_SIZE = 0x300000
        private val EB_WRONG_MD5 = mapOf(
            "8c28ce81c7d359cf9ccaa00d41f8ad33" to "patch/ebp/wrong1.ips",
            "b2dcafd3252cc4697bf4b89ea3358cd5" to "patch/ebp/wrong2.ips",
            "0b8c04fc0182e380ff0e3fe8fdd3b183" to "patch/ebp/wrong3.ips",
            "2225f8a979296b7dcccdda17b6a4f575" to "patch/ebp/wrong4.ips",
            "eb83b9b6ea5692cefe06e54ea3ec9394" to "patch/ebp/wrong5.ips",
            "cc9fa297e7bf9af21f7f179e657f1aa1" to "patch/ebp/wrong6.ips",
        )
    }
}