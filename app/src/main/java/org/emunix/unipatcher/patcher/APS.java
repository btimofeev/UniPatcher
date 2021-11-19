/*
Copyright (C) 2017, 2021 Boris Timofeev

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

package org.emunix.unipatcher.patcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.utils.FileUtils;
import org.emunix.unipatcher.helpers.ResourceProvider;

public class APS extends Patcher {

    public static final int NOT_APS_PATCH = 0;
    public static final int APS_N64_PATCH = 1;
    public static final int APS_GBA_PATCH = 2;

    private static final byte[] APS_N64_MAGIC = {0x41, 0x50, 0x53, 0x31, 0x30}; // APS10
    private static final byte[] APS_GBA_MAGIC = {0x41, 0x50, 0x53, 0x31};       // APS1

    public APS(File patch, File rom, File output, ResourceProvider resourceProvider, FileUtils fileUtils) {
        super(patch, rom, output, resourceProvider, fileUtils);
    }

    @Override
    public void apply(boolean ignoreChecksum) throws PatchException, IOException {
        Patcher aps = null;
        switch (checkAPS(patchFile)) {
            case APS_N64_PATCH:
                aps = new APS_N64(patchFile, romFile, outputFile, resourceProvider, fileUtils);
                break;
            case APS_GBA_PATCH:
                aps = new APS_GBA(patchFile, romFile, outputFile, resourceProvider, fileUtils);
                break;
            case NOT_APS_PATCH:
                throw new PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch));
        }

        aps.apply(ignoreChecksum);
    }

    public int checkAPS(File file) throws PatchException, IOException {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            byte[] magicN64 = new byte[5];
            int count = stream.read(magicN64);
            if (count < 5)
                throw new PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch));
            if (Arrays.equals(magicN64, APS_N64_MAGIC)) {
                return APS_N64_PATCH;
            } else {
                byte[] magicGBA = new byte[4];
                System.arraycopy(magicN64, 0, magicGBA, 0, 4);
                if (Arrays.equals(magicGBA, APS_GBA_MAGIC)) {
                    return APS_GBA_PATCH;
                }
            }
        } finally {
            fileUtils.closeQuietly(stream);
        }
        return NOT_APS_PATCH;
    }
}
