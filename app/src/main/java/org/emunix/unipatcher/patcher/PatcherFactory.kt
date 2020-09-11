/*
 Copyright (c) 2020 Boris Timofeev

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
import org.apache.commons.io.FilenameUtils
import org.emunix.unipatcher.R
import java.io.File
import java.util.*

object PatcherFactory {

    fun createPatcher(context: Context, patch: File, rom: File, output: File): Patcher {
        return when (FilenameUtils.getExtension(patch.name).toLowerCase(Locale.getDefault())) {
            "ips" -> IPS(context, patch, rom, output)
            "ups" -> UPS(context, patch, rom, output)
            "bps" -> BPS(context, patch, rom, output)
            "ppf" -> PPF(context, patch, rom, output)
            "aps" -> APS(context, patch, rom, output)
            "ebp" -> EBP(context, patch, rom, output)
            "dps" -> DPS(context, patch, rom, output)
            "xdelta", "xdelta3", "xd", "vcdiff" -> XDelta(context, patch, rom, output)
            else -> throw PatchException(context.getString(R.string.notify_error_unknown_patch_format))
        }
    }
}