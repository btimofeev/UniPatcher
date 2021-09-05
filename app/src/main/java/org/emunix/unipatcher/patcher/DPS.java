/*
Copyright (C) 2014, 2016, 2021 Boris Timofeev

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

import android.content.Context;

import org.apache.commons.io.IOUtils;
import org.emunix.unipatcher.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.emunix.unipatcher.helpers.ResourceProvider;

public class DPS extends Patcher {

    private static final int MIN_SIZE_PATCH = 136;
    private static final int BUFFER_SIZE = 32768;
    private static final int COPY_DATA = 0;
    private static final int ENCLOSED_DATA = 1;

    public DPS(File patch, File rom, File output, ResourceProvider resourceProvider) {
        super(patch, rom, output, resourceProvider);
    }

    @Override
    public void apply(boolean ignoreChecksum) throws PatchException, IOException {

        if (patchFile.length() < MIN_SIZE_PATCH) {
            throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
        }

        BufferedInputStream patchStream = null;
        RandomAccessFile romStream = null;
        RandomAccessFile outputStream = null;

        try {
            patchStream = new BufferedInputStream(new FileInputStream(patchFile));

            byte[] buffer = new byte[BUFFER_SIZE];

            // check version of dps patch
            long i = patchStream.read(buffer, 0, 198);
            if (buffer[193] != 1)
                throw new PatchException(resourceProvider.getString(R.string.notify_error_not_dps_patch));

            // verify rom
            if (!ignoreChecksum) {
                long romSize = getUInt(buffer, 194);
                if (romSize != romFile.length())
                    throw new IOException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch));
            }

            romStream = new RandomAccessFile(romFile, "r");
            outputStream = new RandomAccessFile(outputFile, "rw");

            int mode;
            long offset;
            long length;
            while ((i = patchStream.read(buffer, 0, 5)) != -1) {
                mode = buffer[0];
                offset = getUInt(buffer, 1);
                outputStream.seek(offset);

                switch (mode) {
                    case COPY_DATA:
                        i = patchStream.read(buffer, 0, 8);
                        offset = getUInt(buffer, 0);
                        length = getUInt(buffer, 4);
                        romStream.seek(offset);
                        while (length > 0) {
                            if (length < BUFFER_SIZE) {
                                i = romStream.read(buffer, 0, (int) length);
                                outputStream.write(buffer, 0, (int) i);
                                length -= i;
                            } else {
                                i = romStream.read(buffer, 0, BUFFER_SIZE);
                                outputStream.write(buffer, 0, (int) i);
                                length -= i;
                            }
                        }
                        break;
                    case ENCLOSED_DATA:
                        i = patchStream.read(buffer, 0, 4);
                        length = getUInt(buffer, 0);
                        while (length > 0) {
                            if (length < BUFFER_SIZE) {
                                i = patchStream.read(buffer, 0, (int) length);
                                outputStream.write(buffer, 0, (int) i);
                                length -= i;
                            } else {
                                i = patchStream.read(buffer, 0, BUFFER_SIZE);
                                outputStream.write(buffer, 0, (int) i);
                                length -= i;
                            }
                        }
                        break;
                }
            }
        } finally {
            IOUtils.closeQuietly(romStream);
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(patchStream);
        }
    }

    private long getUInt(byte[] a, int offset) {
        return ((long) (a[offset] & 0xff)) + ((long) (a[offset + 1] & 0xff) << 8) +
                ((long) (a[offset + 2] & 0xff) << 16) + ((long) (a[offset + 3] & 0xff) << 24);
    }
}
