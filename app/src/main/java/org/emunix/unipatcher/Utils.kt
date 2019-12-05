/*
Copyright (C) 2013-2017, 2019 Boris Timofeev

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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.StatFs
import androidx.core.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Arrays
import java.util.Locale
import kotlin.math.roundToInt

object Utils {
    private const val LOG_TAG = "Utils"

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
            Log.e(LOG_TAG, "App version is not available")
        }

        return versionName
    }

    fun hasStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun dpToPx(context: Context, dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    fun getFreeSpace(file: File): Long {
        val stat = StatFs(file.path)
        return stat.availableBytes
    }

    @Throws(IOException::class)
    fun copyFile(context: Context, from: File, to: File) {
        val dir = to.parentFile ?: throw IllegalArgumentException("Couldn't find parent file: $to")

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
    fun moveFile(context: Context, from: File, to: File) {
        FileUtils.deleteQuietly(to)
        if (!from.renameTo(to)) {
            copyFile(context, from, to)
            FileUtils.deleteQuietly(from)
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

    fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (i in bytes.indices) {
            sb.append("%x".format(bytes[i]))
        }
        return sb.toString()
    }

    fun isPatch(file: File): Boolean {
        val patches = arrayOf("ips", "ups", "bps", "aps", "ppf", "dps", "ebp", "xdelta", "xdelta3", "xd", "vcdiff")
        val ext = FilenameUtils.getExtension(file.name).toLowerCase(Locale.getDefault())
        for (patch in patches) {
            if (ext == patch) return true
        }
        return false
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
