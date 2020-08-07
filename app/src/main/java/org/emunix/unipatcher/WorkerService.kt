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

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.emunix.unipatcher.helpers.UriParser
import org.emunix.unipatcher.patcher.*
import org.emunix.unipatcher.tools.SmdFixChecksum
import org.emunix.unipatcher.tools.SnesSmcHeader
import org.emunix.unipatcher.ui.activity.MainActivity
import org.emunix.unipatcher.ui.notify.*
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*
import javax.inject.Inject

class WorkerService : IntentService("WorkerService") {

    @Inject lateinit var uriParser: UriParser

    override fun onHandleIntent(intent: Intent?) {
        UniPatcher.appComponent.inject(this)

        // if user deny write storage permission
        if (!Utils.hasStoragePermission(this)) {
            showErrorNotification(getString(R.string.permissions_storage_error_notify_access_denied))
            return
        }

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UniPatcher:service")
        wakeLock.acquire()

        try {
            when (intent!!.getIntExtra("action", 0)) {
                Action.APPLY_PATCH -> actionPatching(intent)
                Action.CREATE_PATCH -> actionCreatePatch(intent)
                Action.SMD_FIX_CHECKSUM -> actionSmdFixChecksum(intent)
                Action.SNES_ADD_SMC_HEADER -> actionSnesAddSmcHeader(intent)
                Action.SNES_DELETE_SMC_HEADER -> actionSnesDeleteSmcHeader(intent)
            }
        } finally {
            wakeLock.release()
        }
    }

    private fun actionPatching(intent: Intent) {
        var errorMsg: String? = null
        val romFile = File(intent.getStringExtra("romPath"))
        val patchFile = File(intent.getStringExtra("patchPath"))
        val outputFile = File(intent.getStringExtra("outputPath"))
        val patcher: Patcher?

        if (!fileExists(patchFile) || !fileExists(romFile))
            return

        // create output dir
        try {
            if (!outputFile.parentFile.exists()) {
                FileUtils.forceMkdirParent(outputFile)
            }
        } catch (e: IOException) {
            val text = getString(R.string.notify_error_unable_to_create_directory, outputFile.parent)
            showErrorNotification(text)
            return
        } catch (e: SecurityException) {
            val text = getString(R.string.notify_error_unable_to_create_directory, outputFile.parent)
            showErrorNotification(text)
            return
        }

        // check access to output dir
        try {
            if (!outputFile.parentFile.canWrite()) {
                val text = getString(R.string.notify_error_unable_to_write_to_directory, outputFile.parent)
                showErrorNotification(text)
                return
            }
        } catch (e: SecurityException) {
            val text = getString(R.string.notify_error_unable_to_write_to_directory, outputFile.parent)
            showErrorNotification(text)
            return
        }

        val ext = FilenameUtils.getExtension(patchFile.name).toLowerCase(Locale.getDefault())
        patcher = when (ext) {
            "ips" -> IPS(this, patchFile, romFile, outputFile)
            "ups" -> UPS(this, patchFile, romFile, outputFile)
            "bps" -> BPS(this, patchFile, romFile, outputFile)
            "ppf" -> PPF(this, patchFile, romFile, outputFile)
            "aps" -> APS(this, patchFile, romFile, outputFile)
            "ebp" -> EBP(this, patchFile, romFile, outputFile)
            "dps" -> DPS(this, patchFile, romFile, outputFile)
            "xdelta", "xdelta3", "xd", "vcdiff" -> XDelta(this, patchFile, romFile, outputFile)
            else -> null
        }

        if (patcher == null) {
            showErrorNotification(getString(R.string.notify_error_unknown_patch_format))
            return
        }

        val notify = PatchingNotify(this, outputFile.name)
        startForeground(notify.id, notify.notifyBuilder.build())

        try {
            patcher.apply(Settings.getIgnoreChecksum())
            Settings.setPatchingSuccessful(true)
        } catch (e: Exception) {
            errorMsg = if (Utils.getFreeSpace(outputFile.parentFile) == 0L) {
                getString(R.string.notify_error_not_enough_space)
            } else {
                e.message
            }
            if (outputFile.isFile) {
                FileUtils.deleteQuietly(outputFile)
            }
        } finally {
            stopForeground(true)
        }
        notify.showResult(errorMsg)
    }

    private fun actionCreatePatch(intent: Intent) {
        var errorMsg: String? = null
        val sourceFile = File(intent.getStringExtra("sourcePath"))
        val modifiedFile = File(intent.getStringExtra("modifiedPath"))
        val patchFile = File(intent.getStringExtra("patchPath"))

        if (!fileExists(sourceFile) || !fileExists(modifiedFile))
            return

        // create output dir
        try {
            if (!patchFile.parentFile.exists()) {
                FileUtils.forceMkdirParent(patchFile)
            }
        } catch (e: IOException) {
            val text = getString(R.string.notify_error_unable_to_create_directory, patchFile.parent)
            showErrorNotification(text)
            return
        } catch (e: SecurityException) {
            val text = getString(R.string.notify_error_unable_to_create_directory, patchFile.parent)
            showErrorNotification(text)
            return
        }

        // check access to output dir
        try {
            if (!patchFile.parentFile.canWrite()) {
                val text = getString(R.string.notify_error_unable_to_write_to_directory, patchFile.parent)
                showErrorNotification(text)
                return
            }
        } catch (e: SecurityException) {
            val text = getString(R.string.notify_error_unable_to_write_to_directory, patchFile.parent)
            showErrorNotification(text)
            return
        }

        val patcher = XDelta(this, patchFile, sourceFile, modifiedFile)

        val notify = CreatePatchNotify(this, patchFile.name)

        startForeground(notify.id, notify.notifyBuilder.build())

        try {
            patcher.create()
            Settings.setPatchingSuccessful(true)
        } catch (e: Exception) {
            errorMsg = if (Utils.getFreeSpace(patchFile.parentFile) == 0L) {
                getString(R.string.notify_error_not_enough_space)
            } else {
                e.message
            }
            FileUtils.deleteQuietly(patchFile)
        } finally {
            stopForeground(true)
        }
        notify.showResult(errorMsg)
    }

    private fun actionSmdFixChecksum(intent: Intent) {
        var errorMsg: String? = null

        val romPath = intent.getStringExtra("romPath")
        require(romPath != null) { "romPath is null" }

        val romUri = Uri.parse(romPath)
        val worker = SmdFixChecksum(this, romUri, uriParser)

        val notify = SmdFixChecksumNotify(this, uriParser.getFileName(romUri) ?: "")
        startForeground(notify.id, notify.notifyBuilder.build())

        try {
            worker.fixChecksum()
        } catch (e: IllegalArgumentException) {
            errorMsg = "Illegal argument of method: ${e.message}"
        } catch (e: Exception) {
            errorMsg = e.message
        } finally {
            stopForeground(true)
        }
        notify.showResult(errorMsg)
    }

    private fun actionSnesAddSmcHeader(intent: Intent) {
        var errorMsg: String? = null

        val romFile = File(intent.getStringExtra("romPath"))
        val headerPath = intent.getStringExtra("headerPath")

        if (!fileExists(romFile))
            return

        val worker = SnesSmcHeader()

        val notify = SnesAddSmcHeaderNotify(this, romFile.name)
        startForeground(notify.id, notify.notifyBuilder.build())

        try {
            if (headerPath == null)
                worker.addSnesSmcHeader(this, romFile)
            else
                worker.addSnesSmcHeader(this, romFile, File(headerPath))
        } catch (e: Exception) {
            errorMsg = if (Utils.getFreeSpace(romFile.parentFile) == 0L) {
                getString(R.string.notify_error_not_enough_space)
            } else {
                e.message
            }
        } finally {
            stopForeground(true)
        }
        notify.showResult(errorMsg)
    }

    private fun actionSnesDeleteSmcHeader(intent: Intent) {
        var errorMsg: String? = null

        val romFile = File(intent.getStringExtra("romPath"))

        if (!fileExists(romFile))
            return

        val worker = SnesSmcHeader()

        val notify = SnesDeleteSmcHeaderNotify(this, romFile.name)
        startForeground(notify.id, notify.notifyBuilder.build())

        try {
            worker.deleteSnesSmcHeader(this, romFile, true)
        } catch (e: Exception) {
            errorMsg = if (Utils.getFreeSpace(romFile.parentFile) == 0L) {
                getString(R.string.notify_error_not_enough_space)
            } else {
                e.message
            }
        } finally {
            stopForeground(true)
        }
        notify.showResult(errorMsg)
    }

    private fun fileExists(f: File): Boolean {
        if (!f.exists() || f.isDirectory) {
            val text = getString(R.string.notify_error_file_not_found) + ": " + f.name
            showErrorNotification(text)
            return false
        }
        return true
    }

    private fun showErrorNotification(text: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notify = NotificationCompat.Builder(this, UniPatcher.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.notify_error))
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_gamepad_variant_white_24dp)
                .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(text))
                .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            nm.notify(32768, notify)
        } else {
            startForeground(32768, notify)
            stopForeground(STOP_FOREGROUND_DETACH)
        }
    }
}