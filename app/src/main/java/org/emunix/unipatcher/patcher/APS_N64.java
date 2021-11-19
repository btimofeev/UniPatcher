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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.utils.UFileUtils;
import org.emunix.unipatcher.helpers.ResourceProvider;

public class APS_N64 extends Patcher {

    private static final byte[] MAGIC_NUMBER = {0x41, 0x50, 0x53, 0x31, 0x30}; // APS10
    private static final int TYPE_SIMPLE_PATCH = 0;
    private static final int TYPE_N64_PATCH = 1;
    private static final int ENCODING_SIMPLE = 0;

    public APS_N64(File patch, File rom, File output, ResourceProvider resourceProvider, UFileUtils fileUtils) {
        super(patch, rom, output, resourceProvider, fileUtils);
    }

    @Override
    public void apply(boolean ignoreChecksum) throws PatchException, IOException {
        BufferedInputStream romStream = null;
        BufferedInputStream patchStream = null;
        BufferedOutputStream outputStream = null;

        try {
            patchStream = new BufferedInputStream(new FileInputStream(patchFile));

            long patchSize = patchFile.length();
            long romSize = romFile.length();
            long outSize;
            int romPos = 0;
            int outPos = 0;
            int patchPos = 0;
            long offset, size;

            // check magic string
            byte[] magic = new byte[5];
            size = patchStream.read(magic);
            if (size != 5 || !Arrays.equals(magic, MAGIC_NUMBER))
                throw new PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch));
            patchPos += 5;

            // read and check type of the patch
            int patchType = patchStream.read();
            if ((patchType != TYPE_SIMPLE_PATCH) && (patchType != TYPE_N64_PATCH))
                throw new PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch));
            patchPos++;

            // check encoding method
            int encoding = patchStream.read();
            if (encoding != ENCODING_SIMPLE)
                throw new PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch));
            patchPos++;

            // skip description
            byte[] description = new byte[50];
            size = patchStream.read(description);
            if (size < 50)
                throw new PatchException(resourceProvider.getString(R.string.notify_error_not_aps_patch));
            patchPos += 50;

            // validate ROM
            if (patchType == TYPE_N64_PATCH) {
                int endianness = patchStream.read();
                int cardID = ((patchStream.read() & 0xff) << 8) + (patchStream.read() & 0xff);
                int country = patchStream.read();
                byte[] crc = new byte[8];
                patchStream.read(crc);
                if (!ignoreChecksum) {
                    if (!validateROM(endianness, cardID, country, crc))
                        throw new PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch));
                }
                // skip bytes for future expansion
                byte[] skip = new byte[5];
                patchStream.read(skip);
                patchPos += 17;
            }

            // read size of destination image.
            outSize = readLELong(patchStream);
            patchPos += 4;

            romStream = new BufferedInputStream(new FileInputStream(romFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            // apply patch
            while (patchPos < patchSize) {
                offset = readLELong(patchStream);
                if (offset < 0)
                    throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
                patchPos += 4;

                // copy data from rom to out
                if (offset <= romSize) {
                    if (outPos < offset) {
                        size = offset - outPos;
                        fileUtils.copy(romStream, outputStream, size);
                        romPos += size;
                        outPos += size;
                    }
                } else {
                    if (outPos < romSize) {
                        size = (int) romSize - outPos;
                        fileUtils.copy(romStream, outputStream, size);
                        romPos += size;
                        outPos += size;
                    }
                    if (outPos < offset) {
                        size = offset - outPos;
                        fileUtils.copy(size, (byte) 0x0, outputStream);
                        outPos += size;
                    }
                }

                // copy data from patch to out
                size = patchStream.read();
                patchPos++;
                if (size != 0) {
                    byte[] data = new byte[(int) size];
                    patchStream.read(data);
                    patchPos += size;
                    outputStream.write(data);
                    outPos += size;
                } else { // RLE
                    byte val = (byte) patchStream.read();
                    size = patchStream.read();
                    patchPos += 2;
                    byte[] data = new byte[(int) size];
                    Arrays.fill(data, val);
                    outputStream.write(data);
                    outPos += size;
                }

                // skip rom data
                if (offset <= romSize) {
                    if (romPos + size > romSize) {
                        romPos = (int) romSize;
                    } else {
                        byte[] buf = new byte[(int) size];
                        romStream.read(buf);
                        romPos += size;
                    }
                }
            }
            // write rom tail and trim
            fileUtils.copy(romStream, outputStream, outSize - outPos);
        } finally {
            fileUtils.closeQuietly(romStream);
            fileUtils.closeQuietly(patchStream);
            fileUtils.closeQuietly(outputStream);
        }
    }

    private boolean validateROM(int endianness, int cartID, int country, byte[] crc) throws IOException {
        RandomAccessFile rom = new RandomAccessFile(romFile, "r");
        int val;
        try {
            // check endianness
            val = rom.read();
            if ((endianness == 1 && val != 0x80) || (endianness == 0 && val != 0x37))
                return false;

            // check cartID
            rom.seek(0x3c);
            if (endianness == 1) {
                val = ((rom.read() & 0xff) << 8) + (rom.read() & 0xff);
            } else {
                val = (rom.read() & 0xff) + ((rom.read() & 0xff) << 8);
            }
            if (cartID != val)
                return false;

            // check country
            val = rom.read();
            if (endianness == 0)
                val = rom.read();
            if (country != val)
                return false;

            // check crc
            byte[] buf = new byte[8];
            rom.seek(0x10);
            rom.read(buf);
            if (endianness == 0) {
                byte tmp;
                for (int i = 0; i < buf.length; i += 2) {
                    tmp = buf[i];
                    buf[i] = buf[i + 1];
                    buf[i + 1] = tmp;
                }
            }
            if (!Arrays.equals(crc, buf))
                return false;
        } finally {
            fileUtils.closeQuietly(rom);
        }
        return true;
    }

    private long readLELong(InputStream stream) throws IOException {
        return (stream.read() & 0xff) + ((stream.read() & 0xff) << 8)
                + ((stream.read() & 0xff) << 16) + ((stream.read() & 0xff) << 24);
    }
}
