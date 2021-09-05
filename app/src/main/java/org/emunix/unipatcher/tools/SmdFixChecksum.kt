/*
Copyright (C) 2014, 2020, 2021 Boris Timofeev

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
package org.emunix.unipatcher.tools

import org.apache.commons.io.IOUtils
import org.emunix.unipatcher.R
import org.emunix.unipatcher.helpers.ResourceProvider
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

class SmdFixChecksum(
    private val smdFile: File,
    private val resourceProvider: ResourceProvider,
) {

    @Throws(RomException::class, IOException::class)
    private fun calculateChecksum(): Int {
        val length = smdFile.length()
        if (length < 514) {
            throw RomException(resourceProvider.getString(R.string.notify_error_not_smd_rom))
        }
        var sum: Long = 0
        val stream = smdFile.inputStream()
        val smdStream = BufferedInputStream(stream)
        try {
            var c = IOUtils.skip(smdStream, 512)
            if (c != 512L) throw IOException("Skip failed")
            var byte1: Int
            var byte2: Int
            while (c < length) {
                byte1 = smdStream.read()
                byte2 = smdStream.read()
                if (byte1 == -1 || byte2 == -1) throw RomException(resourceProvider.getString(R.string.notify_error_unexpected_end_of_file))
                sum += (byte1 shl 8) + byte2.toLong()
                c += 2
            }
        } finally {
            IOUtils.closeQuietly(smdStream)
            IOUtils.closeQuietly(stream)
        }
        return sum.toInt() and 0xffff
    }

    @Throws(IOException::class)
    private fun writeChecksum(sum: Int) {
        val data = byteArrayOf((sum shr 8 and 0xff).toByte(), (sum and 0xff).toByte())

        val smdStream = RandomAccessFile(smdFile, "rw")
        try {
            smdStream.seek(CHECKSUM_OFFSET.toLong())
            smdStream.write(data)
        } finally {
            IOUtils.closeQuietly(smdStream)
        }
    }

    @Throws(RomException::class, IOException::class)
    fun fixChecksum() {
        val sum = calculateChecksum()
        Timber.d("SMD checksum: #%x", sum)
        writeChecksum(sum)
    }

    companion object {
        const val CHECKSUM_OFFSET = 0x18e
    }

}