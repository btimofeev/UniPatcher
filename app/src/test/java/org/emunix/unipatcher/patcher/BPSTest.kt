/*
 Copyright (c) 2026 Boris Timofeev

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

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.emunix.unipatcher.R
import org.emunix.unipatcher.helpers.ResourceProvider
import org.emunix.unipatcher.utils.FileUtils
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class BPSTest {

    private companion object {
        const val PATCH_CORRUPTED = "The patch file is corrupted."
    }

    @get:Rule
    val folder = TemporaryFolder()

    private val resourceProvider: ResourceProvider = mockk()
    private val context: Context = mockk()

    private lateinit var fileUtils: FileUtils

    @Before
    fun setUp() {
        every { resourceProvider.getString(R.string.notify_error_patch_corrupted) }
            .returns(PATCH_CORRUPTED)
        fileUtils = FileUtils(context, resourceProvider)
    }

    @Test
    fun testApply() {
        assertTrue(applyPatch("/bps/1.bps", "/bps/1.bin", "/bps/1m.bin"))
    }

    private fun applyPatch(patchName: String, origName: String, modifiedName: String): Boolean {
        val patch = File(javaClass.getResource(patchName)!!.path)
        val origFile = File(javaClass.getResource(origName)!!.path)
        val out = folder.newFile("out.bin")

        val patcher = BPS(patch, origFile, out, resourceProvider, fileUtils)
        try {
            patcher.apply(false)
        } catch (e: PatchException) {
            fail("Patching failed")
        } catch (e: IOException) {
            fail("Patching failed")
        }

        val origOut = File(javaClass.getResource(modifiedName)!!.path)
        return fileUtils.checksumCRC32(out) == fileUtils.checksumCRC32(origOut)
    }
}