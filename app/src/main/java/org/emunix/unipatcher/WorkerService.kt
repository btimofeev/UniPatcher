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
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.DocumentsContract
import org.apache.commons.io.FileUtils
import org.emunix.unipatcher.helpers.UriParser
import org.emunix.unipatcher.patcher.*
import org.emunix.unipatcher.tools.CreateXDelta3
import org.emunix.unipatcher.tools.SmdFixChecksum
import org.emunix.unipatcher.tools.SnesSmcHeader
import org.emunix.unipatcher.ui.notify.*
import java.io.File
import javax.inject.Inject

class WorkerService : IntentService("WorkerService") {

    @Inject lateinit var uriParser: UriParser

    override fun onHandleIntent(intent: Intent?) {
        UniPatcher.appComponent.inject(this)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UniPatcher:service")
        wakeLock.acquire(30*60*1000L /*30 minutes*/)

        try {
            when (intent!!.getIntExtra("action", 0)) {
                Action.APPLY_PATCH -> actionPatching(intent)
                Action.CREATE_PATCH -> actionCreatePatch(intent)
                Action.SMD_FIX_CHECKSUM -> actionSmdFixChecksum(intent)
                Action.SNES_REMOVE_SMC_HEADER -> actionSnesDeleteSmcHeader(intent)
            }
        } finally {
            wakeLock.release()
        }
    }

    private fun actionPatching(intent: Intent) {
        var errorMsg: String? = null
        val romPath = intent.getStringExtra("romPath")
        val patchPath = intent.getStringExtra("patchPath")
        val outputPath = intent.getStringExtra("outputPath")
        require(romPath != null) { "romPath is null" }
        require(patchPath != null) { "patchPath is null" }
        require(outputPath != null) { "outputPath is null" }
        val romUri = Uri.parse(romPath)
        val patchUri = Uri.parse(patchPath)
        val outputUri = Uri.parse(outputPath)

        val notify = PatchingNotify(this, uriParser.getFileName(romUri) ?: "")
        startForeground(notify.id, notify.notifyBuilder.build())

        var romFile: File? = null
        var patchFile: File? = null
        var outputFile: File? = null

        try {
            romFile = Utils.copyToTempFile(this, romUri)
            patchFile = Utils.copyToTempFile(this, patchUri, uriParser.getFileName(patchUri) ?: "undefined")
            outputFile = File.createTempFile("output", ".rom", Utils.getTempDir(this))
            val patcher = PatcherFactory.createPatcher(this, patchFile, romFile, outputFile)
            patcher.apply(Settings.getIgnoreChecksum())
            Utils.copy(outputFile, outputUri, this)
            Settings.setPatchingSuccessful(true)
        } catch (e: Exception) {
            errorMsg = e.message
            if (uriParser.isExist(outputUri))
                DocumentsContract.deleteDocument(contentResolver, outputUri)
        } finally {
            FileUtils.deleteQuietly(outputFile)
            FileUtils.deleteQuietly(romFile)
            FileUtils.deleteQuietly(patchFile)
            stopForeground(notify)
        }
        notify.showResult(errorMsg)
    }

    private fun actionCreatePatch(intent: Intent) {
        var errorMsg: String? = null
        val sourcePath = intent.getStringExtra("sourcePath")
        val modifiedPath = intent.getStringExtra("modifiedPath")
        val patchPath = intent.getStringExtra("patchPath")
        require(sourcePath != null) { "sourcePath is null" }
        require(modifiedPath != null) { "modifiedPath is null" }
        require(patchPath != null) { "patchPath is null" }

        val sourceUri = Uri.parse(sourcePath)
        val modifiedUri = Uri.parse(modifiedPath)
        val patchUri = Uri.parse(patchPath)

        val notify = CreatePatchNotify(this, uriParser.getFileName(patchUri))
        startForeground(notify.id, notify.notifyBuilder.build())

        val patchMaker = CreateXDelta3(this, patchUri, sourceUri, modifiedUri, uriParser)

        try {
            patchMaker.create()
            Settings.setPatchingSuccessful(true)
        } catch (e: Exception) {
            errorMsg = e.message
            DocumentsContract.deleteDocument(contentResolver, patchUri)
        } finally {
            stopForeground(notify)
        }
        notify.showResult(errorMsg)
    }

    private fun actionSmdFixChecksum(intent: Intent) {
        var errorMsg: String? = null
        val romPath = intent.getStringExtra("romPath")
        require(romPath != null) { "romPath is null" }
        val romUri = Uri.parse(romPath)

        val notify = SmdFixChecksumNotify(this, uriParser.getFileName(romUri) ?: "")
        startForeground(notify.id, notify.notifyBuilder.build())

        var tmpFile: File? = null
        try {
            tmpFile = Utils.copyToTempFile(this, romUri)
            val worker = SmdFixChecksum(this, tmpFile)
            worker.fixChecksum()
            Utils.copy(tmpFile, romUri, this)
        } catch (e: Exception) {
            errorMsg = e.message
            FileUtils.deleteQuietly(tmpFile)
        } finally {
            stopForeground(notify)
        }
        notify.showResult(errorMsg)
    }

    private fun actionSnesDeleteSmcHeader(intent: Intent) {
        var errorMsg: String? = null

        val romPath = intent.getStringExtra("romPath")
        val outputPath = intent.getStringExtra("outputPath")
        require(romPath != null) { "romPath is null" }
        require(outputPath != null) { "outputPath is null" }
        val romUri = Uri.parse(romPath)
        val outputUri = Uri.parse(outputPath)

        val notify = SnesDeleteSmcHeaderNotify(this, uriParser.getFileName(romUri) ?: "")
        startForeground(notify.id, notify.notifyBuilder.build())

        var romFile: File? = null
        var outputFile: File? = null

        try {
            romFile = Utils.copyToTempFile(this, romUri)
            outputFile = Utils.copyToTempFile(this, outputUri)
            SnesSmcHeader().deleteSnesSmcHeader(this, romFile, outputFile)
            Utils.copy(outputFile, outputUri, this)
        } catch (e: Exception) {
            errorMsg = e.message
            if (uriParser.isExist(outputUri))
                DocumentsContract.deleteDocument(contentResolver, outputUri)
        } finally {
            FileUtils.deleteQuietly(outputFile)
            FileUtils.deleteQuietly(romFile)
            stopForeground(notify)
        }
        notify.showResult(errorMsg)
    }

    private fun stopForeground(notify: Notify) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            stopForeground(true)
            notify.setSticked(false)
            notify.show()
        } else {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
    }
}