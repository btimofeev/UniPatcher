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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class IPSTest {

    private companion object {
        const val NOT_IPS_PATCH = "Not an IPS patch."
    }

    @get:Rule
    val folder = TemporaryFolder()

    private val resourceProvider: ResourceProvider = mockk()
    private val context: Context = mockk()

    private lateinit var fileUtils: FileUtils

    @Before
    fun setUp() {
        every { resourceProvider.getString(R.string.notify_error_not_ips_patch) }
            .returns(NOT_IPS_PATCH)
        fileUtils = FileUtils(context, resourceProvider)
    }

    @Test
    fun IPS_InvalidPatch_NoMagic() {
        val patch = File(javaClass.getResource("/ips/not_ips.ips")!!.path)
        val origFile = File(javaClass.getResource("/ips/min_ips.bin")!!.path)
        val out = folder.newFile("out.bin")

        val patcher = IPS(patch, origFile, out, resourceProvider, fileUtils)

        try {
            patcher.apply()
            fail("Expected an PatchException to be thrown")
        } catch (e: PatchException) {
            assertEquals("Not an IPS patch.", e.message)
        }
    }

    @Test
    fun IPS_MinPatch() {
        assertTrue(applyPatch("/ips/min_ips.ips", "/ips/min_ips.bin", "/ips/min_ips_modified.bin"))
    }

    @Test
    fun IPS_RlePatch() {
        assertTrue(applyPatch("/ips/rle_ips.ips", "/ips/rle_ips.bin", "/ips/rle_ips_modified.bin"))
    }

    @Test
    fun IPS_ExtendPatch() {
        assertTrue(applyPatch("/ips/extend_ips.ips", "/ips/extend_ips.bin", "/ips/extend_ips_modified.bin"))
    }

    @Test
    fun IPS_TruncateRom() {
        assertTrue(applyPatch("/ips/truncate.ips", "/ips/truncate.bin", "/ips/truncate_modified.bin"))
    }

    @Test
    fun IPS32_MinPatch() {
        assertTrue(applyPatch("/ips/min_ips32.ips", "/ips/min_ips32.bin", "/ips/min_ips32_mod.bin"))
    }

    @Test
    fun IPS32_RlePatch() {
        assertTrue(applyPatch("/ips/rle_ips32.ips", "/ips/rle_ips32.bin", "/ips/rle_ips32_mod.bin"))
    }

    @Test
    fun IPS32_ExtendPatch() {
        assertTrue(applyPatch("/ips/extend_ips32.ips", "/ips/extend_ips32.bin", "/ips/extend_ips32_mod.bin"))
    }

    private fun applyPatch(patchName: String, origName: String, modifiedName: String): Boolean {
        val patch = File(javaClass.getResource(patchName)!!.path)
        val origFile = File(javaClass.getResource(origName)!!.path)
        val out = folder.newFile("out.bin")

        val patcher = IPS(patch, origFile, out, resourceProvider, fileUtils)
        try {
            patcher.apply()
        } catch (e: PatchException) {
            fail("Patching failed")
        } catch (e: IOException) {
            fail("Patching failed")
        }

        val origOut = File(javaClass.getResource(modifiedName)!!.path)
        return fileUtils.checksumCRC32(out) == fileUtils.checksumCRC32(origOut)
    }
}