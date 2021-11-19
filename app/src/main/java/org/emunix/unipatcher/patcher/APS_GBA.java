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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.utils.UFileUtils;
import org.emunix.unipatcher.helpers.ResourceProvider;
import org.emunix.unipatcher.utils.Crc16;

public class APS_GBA extends Patcher {
    private static final byte[] MAGIC_NUMBER = {0x41, 0x50, 0x53, 0x31}; // APS1
    private static final int CHUNK_SIZE = 65536;

    public APS_GBA(File patch, File rom, File output, ResourceProvider resourceProvider, UFileUtils fileUtils) {
        super(patch, rom, output, resourceProvider, fileUtils);
    }

    @Override
    public void apply(boolean ignoreChecksum) throws PatchException, IOException {
        long fileSize1, fileSize2, bytesLeft, offset;
        int crc, patchCrc1, patchCrc2, pCount, oCount;
        boolean isOriginal = false;
        boolean isModified = false;

        byte[] romBuf = new byte[CHUNK_SIZE];
        byte[] patchBuf = new byte[CHUNK_SIZE];

        BufferedInputStream patchStream = null;
        RandomAccessFile output = null;

        fileUtils.copyFile(romFile, outputFile);

        try {
            patchStream = new BufferedInputStream(new FileInputStream(patchFile));
            output = new RandomAccessFile(outputFile, "rw");

            byte[] magic = new byte[4];
            pCount = patchStream.read(magic);
            if (pCount < 4 || !Arrays.equals(magic, MAGIC_NUMBER))
                throw new PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch));

            fileSize1 = readLEInt(patchStream);
            fileSize2 = readLEInt(patchStream);
            if (fileSize1 < 0 || fileSize2 < 0)
                throw new PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch));

            bytesLeft = patchFile.length() - 12;

            while (bytesLeft > 0) {
                offset = readLEInt(patchStream);
                patchCrc1 = readLEChar(patchStream);
                patchCrc2 = readLEChar(patchStream);
                bytesLeft -= 8;
                if (offset < 0 || patchCrc1 < 0 || patchCrc2 < 0)
                    throw new PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch));

                output.seek(offset);
                oCount = output.read(romBuf);
                pCount = patchStream.read(patchBuf);
                bytesLeft -= CHUNK_SIZE;
                if (pCount < CHUNK_SIZE)
                    throw new PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch));

                if (oCount < CHUNK_SIZE) {
                    if (oCount < 0) oCount = 0;
                    for (int i = oCount; i < CHUNK_SIZE; i++)
                        romBuf[i] = 0x0;
                }

                crc = new Crc16().calculate(romBuf);

                for (int i = 0; i < CHUNK_SIZE; i++)
                    romBuf[i] ^= patchBuf[i];

                if (crc == patchCrc1) {
                    isOriginal = true;
                } else if (crc == patchCrc2) {
                    isModified = true;
                } else {
                    if (!ignoreChecksum)
                        throw new PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch));
                }
                if (isOriginal && isModified)
                    throw new PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch));

                output.seek(offset);
                output.write(romBuf);
            }
        } finally {
            fileUtils.closeQuietly(patchStream);
            fileUtils.closeQuietly(output);
        }

        if (isOriginal) {
            fileUtils.truncateFile(outputFile, fileSize2);
        } else if (isModified) {
            fileUtils.truncateFile(outputFile, fileSize1);
        }
    }

    private long readLEInt(InputStream stream) throws IOException {
        long result = 0;
        int x;
        for (int i = 0; i < 4; i++) {
            x = stream.read();
            if (x == -1)
                return -1;
            result += ((long) x) << (i * 8);
        }
        return result;
    }

    private int readLEChar(InputStream stream) throws IOException {
        int result = 0;
        int x;
        for (int i = 0; i < 2; i++) {
            x = stream.read();
            if (x == -1)
                return -1;
            result += x << (i * 8);
        }
        return result;
    }
}
