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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class SnesSmcHeader {
    fun isRomHasSmcHeader(romSize: Long): Boolean {
        return romSize and 0x7fff == 512L
    }

    /**
     * Copy [romFile] without SMC header to [outputFile]
     * @param romFile the file from which you want to remove the smc header
     * @param outputFile the file in which you want to save rom without smc header
     * @param resourceProvider application resource provider
     * @throws RomException if [romFile] has no SMC header
     */
    @Throws(IOException::class, RomException::class)
    fun deleteSnesSmcHeader(romFile: File, outputFile: File, resourceProvider: ResourceProvider, ) {
        if (!isRomHasSmcHeader(romFile.length())) {
            throw RomException(resourceProvider.getString(R.string.snes_rom_has_no_smc_header))
        }

        val inputStream = FileInputStream(romFile)
        val outputStream = FileOutputStream(outputFile)

        inputStream.use { input ->
            outputStream.use { output ->
                IOUtils.skipFully(input, SMC_HEADER_SIZE.toLong())
                IOUtils.copy(input, output)
            }
        }
    }

    companion object {
        private const val SMC_HEADER_SIZE = 512
    }
}