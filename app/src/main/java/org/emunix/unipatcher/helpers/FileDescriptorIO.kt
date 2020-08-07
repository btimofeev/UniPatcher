/*
 Copyright (c) 2020 Boris Timofeev

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

package org.emunix.unipatcher.helpers

import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import timber.log.Timber
import java.io.FileDescriptor
import java.io.IOException

class FileDescriptorIO(private val fd: FileDescriptor) {

    @Throws(IllegalArgumentException::class, IOException::class)
    fun write(bytes: ByteArray, byteOffset: Int, byteCount: Int) {
        if (byteOffset or byteCount < 0 || byteOffset > bytes.size || bytes.size - byteOffset < byteCount)
            throw IllegalArgumentException("byteArray.size=${bytes.size}, offset=${byteOffset}, count=${byteCount}")

        if (byteCount == 0) return

        try {
            var count = byteCount
            var offset = byteOffset
            while (count > 0) {
                val bytesWritten = Os.write(fd, bytes, offset, count)
                count -= bytesWritten
                offset += bytesWritten
            }
        } catch (e: ErrnoException) {
            throw IOException(e.message)
        } catch (e: InterruptedException) {
            throw IOException(e.message)
        }
    }

    @Throws(IOException::class)
    fun seek(offset: Long): Long {
        try {
            return Os.lseek(fd, offset, OsConstants.SEEK_SET)
        } catch (e: ErrnoException) {
            throw IOException(e.message)
        }
    }

    fun close() {
        try {
            Os.close(fd)
        } catch (e: ErrnoException) {
            Timber.e(e)
        }
    }
}