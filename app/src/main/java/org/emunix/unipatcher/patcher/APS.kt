/*
Copyright (C) 2017, 2021, 2024 Boris Timofeev

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
import org.emunix.unipatcher.patcher.APS.PatchType.APS_GBA_PATCH
import org.emunix.unipatcher.patcher.APS.PatchType.APS_N64_PATCH
import org.emunix.unipatcher.patcher.APS.PatchType.NOT_APS_PATCH
import org.emunix.unipatcher.utils.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class APS(
    patch: File?,
    rom: File?,
    output: File?,
    resourceProvider: ResourceProvider?,
    fileUtils: FileUtils?
) : Patcher(patch, rom, output, resourceProvider, fileUtils) {

    @Throws(PatchException::class, IOException::class)
    override fun apply(ignoreChecksum: Boolean) {
        val aps = when (checkAPS(patchFile)) {
            APS_N64_PATCH -> APS_N64(patchFile, romFile, outputFile, resourceProvider, fileUtils)
            APS_GBA_PATCH -> APS_GBA(patchFile, romFile, outputFile, resourceProvider, fileUtils)
            NOT_APS_PATCH -> throw PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch))
        }

        aps.apply(ignoreChecksum)
    }

    @Throws(PatchException::class, IOException::class)
    private fun checkAPS(file: File?): PatchType {
        FileInputStream(file).use { stream ->
            val magicN64 = ByteArray(APS_N64_MAGIC.size)
            val count = stream.read(magicN64)
            if (count < APS_N64_MAGIC.size) throw PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch))
            if (magicN64.contentEquals(APS_N64_MAGIC)) {
                return APS_N64_PATCH
            }

            val magicGBA = ByteArray(APS_GBA_MAGIC.size)
            System.arraycopy(magicN64, 0, magicGBA, 0, APS_GBA_MAGIC.size)
            if (magicGBA.contentEquals(APS_GBA_MAGIC)) {
                return APS_GBA_PATCH
            }
        }
        return NOT_APS_PATCH
    }

    private enum class PatchType {
        NOT_APS_PATCH,
        APS_N64_PATCH,
        APS_GBA_PATCH
    }

    private companion object {

        private val APS_N64_MAGIC = byteArrayOf(0x41, 0x50, 0x53, 0x31, 0x30) // APS10
        private val APS_GBA_MAGIC = byteArrayOf(0x41, 0x50, 0x53, 0x31) // APS1
    }
}
