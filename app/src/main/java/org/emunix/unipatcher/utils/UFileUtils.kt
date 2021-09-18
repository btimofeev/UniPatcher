/*
Copyright (C) 2013-2017, 2019-2021 Boris Timofeev

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

package org.emunix.unipatcher.utils

import android.content.Context
import android.net.Uri
import android.os.StatFs
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import org.emunix.unipatcher.R.string
import org.emunix.unipatcher.helpers.ResourceProvider
import java.io.*
import java.util.*
import javax.inject.Inject

class UFileUtils @Inject constructor(
    private val context: Context,
    private val resourceProvider: ResourceProvider,
) {

    fun getTempDir(): File = resourceProvider.tempDir

    fun getFreeSpace(file: File): Long {
        val stat = StatFs(file.path)
        return stat.availableBytes
    }

    @Throws(IOException::class)
    fun copyFile(input: File, output: File) {
        val dir = output.parentFile ?: throw IOException("Couldn't find parent file: $output")

        if (getFreeSpace(dir) < input.length()) {
            throw IOException(resourceProvider.getString(string.notify_error_not_enough_space))
        }

        try {
            input.copyTo(target = output, overwrite = true)
        } catch (e: IOException) {
            throw IOException(resourceProvider.getString(string.notify_error_could_not_copy_file))
        } catch (e: NoSuchFileException) {
            throw IOException(resourceProvider.getString(string.notify_error_could_not_copy_file))
        }
    }

    @Throws(IOException::class)
    fun truncateFile(f: File, size: Long) {
        FileOutputStream(f, true)
            .channel
            .use { it.truncate(size) }
    }

    @Throws(IOException::class)
    fun copy(from: InputStream, to: OutputStream, size: Long) {
        var bytes = size
        val buffer = ByteArray(BUFFER_SIZE)
        var c: Int
        while (bytes > 0) {
            c = if (bytes < BUFFER_SIZE) {
                from.read(buffer, 0, bytes.toInt())
            } else {
                from.read(buffer)
            }
            if (c != -1) {
                to.write(buffer, 0, c)
                bytes -= c.toLong()
            } else {
                copy(bytes, 0x0.toByte(), to)
                bytes = 0
            }
        }
    }

    @Throws(IOException::class)
    fun copy(number: Long, b: Byte, to: OutputStream) {
        var count = number
        val buffer = ByteArray(BUFFER_SIZE)
        Arrays.fill(buffer, b)
        while (count > 0) {
            if (count >= BUFFER_SIZE) {
                to.write(buffer)
                count -= BUFFER_SIZE.toLong()
            } else {
                to.write(buffer, 0, count.toInt())
                count = 0
            }
        }
    }

    @Throws(IOException::class)
    suspend fun copyToTempFile(inputStream: InputStream, ext: String = ".tmp"): File =
        withContext(Dispatchers.IO) {
            val tmpDir = getTempDir()
            val tmpFile = File.createTempFile("file", ext, tmpDir)
            val outputStream = tmpFile.outputStream()
            outputStream.use {
                IOUtils.copy(inputStream, it)
            }
            return@withContext tmpFile
        }

    @Throws(IOException::class, FileNotFoundException::class)
    suspend fun copyToTempFile(uri: Uri, ext: String = ".tmp"): File = withContext(Dispatchers.IO) {
        val stream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open ${uri}: content resolver returned null")
        try {
            return@withContext copyToTempFile(stream, ext)
        } finally {
            IOUtils.closeQuietly(stream)
        }
    }

    @Throws(IOException::class)
    fun copy(from: File, to: Uri) {
        val inputStream = from.inputStream()
        val outputStream = context.contentResolver.openOutputStream(to)
            ?: throw IOException("Unable to open ${to}: content resolver returned null")
        try {
            IOUtils.copy(inputStream, outputStream)
        } finally {
            IOUtils.closeQuietly(inputStream)
            IOUtils.closeQuietly(outputStream)
        }
    }

    fun getFileName(uri: Uri): String = DocumentFile.fromSingleUri(context, uri)?.name ?: "Undefined name"

    fun getFileSize(uri: Uri): Long? = DocumentFile.fromSingleUri(context, uri)?.length()

    companion object {

        private const val BUFFER_SIZE = 10240 // 10 Kb
    }
}
