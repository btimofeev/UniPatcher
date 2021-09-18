/*
 Copyright (c) 2020-2021 Boris Timofeev

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

import org.apache.commons.io.FilenameUtils
import org.emunix.unipatcher.R
import org.emunix.unipatcher.utils.UFileUtils
import org.emunix.unipatcher.helpers.ResourceProvider
import java.io.File
import java.util.Locale
import javax.inject.Inject

class PatcherFactory @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val fileUtils: UFileUtils,
) {

    fun createPatcher(patch: File, rom: File, output: File): Patcher {
        return when (FilenameUtils.getExtension(patch.name).lowercase(Locale.getDefault())) {
            "ips" -> IPS(patch, rom, output, resourceProvider, fileUtils)
            "ups" -> UPS(patch, rom, output, resourceProvider, fileUtils)
            "bps" -> BPS(patch, rom, output, resourceProvider, fileUtils)
            "ppf" -> PPF(patch, rom, output, resourceProvider, fileUtils)
            "aps" -> APS(patch, rom, output, resourceProvider, fileUtils)
            "ebp" -> EBP(patch, rom, output, resourceProvider, fileUtils)
            "dps" -> DPS(patch, rom, output, resourceProvider, fileUtils)
            "xdelta", "xdelta3", "xd", "vcdiff" -> XDelta(patch, rom, output, resourceProvider, fileUtils)
            else -> throw PatchException(resourceProvider.getString(R.string.notify_error_unknown_patch_format))
        }
    }
}