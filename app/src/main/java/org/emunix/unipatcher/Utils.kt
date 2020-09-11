/*
Copyright (C) 2013-2017, 2019-2020 Boris Timofeev

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

package org.emunix.unipatcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.StatFs
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import timber.log.Timber
import java.io.*
import java.util.*

object Utils {

    private const val BUFFER_SIZE = 10240 // 10 Kb

    fun startForegroundService(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            context.startService(intent)
        } else {
            context.startForegroundService(intent)
        }
    }

    fun getAppVersion(context: Context): String {
        var versionName = "N/A"
        try {
            val pinfo = context.packageManager.getPackageInfo(context.packageName, 0)
            versionName = pinfo.versionName
        } catch (e: Exception) {
            Timber.e("App version is not available")
        }

        return versionName
    }

    fun getTempDir(context: Context): File {
        return context.externalCacheDir ?: context.cacheDir
    }

    fun getFreeSpace(file: File): Long {
        val stat = StatFs(file.path)
        return stat.availableBytes
    }

    @Throws(IOException::class)
    fun copyFile(context: Context, from: File, to: File) {
        val dir = to.parentFile ?: throw IOException("Couldn't find parent file: $to")

        if (getFreeSpace(dir) < from.length()) {
            throw IOException(context.getString(R.string.notify_error_not_enough_space))
        }

        try {
            FileUtils.copyFile(from, to)
        } catch (e: IOException) {
            throw IOException(context.getString(R.string.notify_error_could_not_copy_file))
        }
    }

    @Throws(IOException::class)
    fun truncateFile(f: File, size: Long) {
        val channel = FileOutputStream(f, true).channel
        channel.truncate(size)
        IOUtils.closeQuietly(channel)
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
    fun copyToTempFile(context: Context, inputStream: InputStream, ext: String = ".tmp"): File {
        val tmpDir = getTempDir(context)
        val tmpFile = File.createTempFile("file", ext, tmpDir)
        val outputStream = tmpFile.outputStream()
        outputStream.use {
            IOUtils.copy(inputStream, it)
        }
        return tmpFile
    }

    @Throws(IOException::class, FileNotFoundException::class)
    fun copyToTempFile(context: Context, uri: Uri, ext: String = ".tmp"): File {
        val stream: InputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Unable to open ${uri}: content resolver returned null")
        try {
            return copyToTempFile(context, stream, ext)
        } finally {
            IOUtils.closeQuietly(stream)
        }
    }

    @Throws(IOException::class)
    fun copy(from: File, to: Uri, context: Context) {
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

    fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (i in bytes.indices) {
            sb.append("%x".format(bytes[i]))
        }
        return sb.toString()
    }

    fun isArchive(path: String): Boolean {
        val ext = FilenameUtils.getExtension(path).toLowerCase(Locale.getDefault())
        return (ext == "zip"
                || ext == "rar"
                || ext == "7z"
                || ext == "gz"
                || ext == "tgz")
    }
}
