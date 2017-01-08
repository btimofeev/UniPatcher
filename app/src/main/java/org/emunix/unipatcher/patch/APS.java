/*
Copyright (C) 2017 Boris Timofeev

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

package org.emunix.unipatcher.patch;

import android.content.Context;

import org.apache.commons.io.IOUtils;
import org.emunix.unipatcher.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class APS extends Patch {

    public static final int NOT_APS_PATCH = 0;
    public static final int APS_N64_PATCH = 1;
    public static final int APS_GBA_PATCH = 2;

    private static final byte[] APS_N64_MAGIC = {0x41, 0x50, 0x53, 0x31, 0x30}; // APS10
    private static final byte[] APS_GBA_MAGIC = {0x41, 0x50, 0x53, 0x31, 0x00}; // APS1

    public APS(Context context, File patch, File rom, File output) {
        super(context, patch, rom, output);
    }

    @Override
    public void apply() throws PatchException, IOException {
        Patch aps = null;
        switch (checkAPS(patchFile)) {
            case APS_N64_PATCH:
                aps = new APS_N64(context, patchFile, romFile, outputFile); break;
            case APS_GBA_PATCH:
                throw new PatchException(context.getString(R.string.notify_error_aps_gba));
            case NOT_APS_PATCH:
                throw new PatchException(context.getString(R.string.notify_error_not_aps_patch));
        }

        aps.apply();
    }

    public int checkAPS(File file) throws IOException {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            byte[] magic = new byte[5];
            stream.read(magic);
            if (Arrays.equals(magic, APS_N64_MAGIC)) {
                return APS_N64_PATCH;
            } else if (Arrays.equals(magic, APS_GBA_MAGIC)) {
                return APS_GBA_PATCH;
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return NOT_APS_PATCH;
    }
}
