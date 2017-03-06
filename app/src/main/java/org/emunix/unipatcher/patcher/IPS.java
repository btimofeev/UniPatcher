/*
Copyright (C) 2013, 2016, 2017 Boris Timofeev

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
import org.emunix.unipatcher.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class IPS extends Patcher {

    public static final int NOT_IPS_PATCH = 0;
    public static final int IPS_PATCH = 1;
    public static final int IPS32_PATCH = 2;

    private static final byte[] MAGIC_NUMBER_IPS = {0x50, 0x41, 0x54, 0x43, 0x48};   // "PATCH"
    private static final byte[] MAGIC_NUMBER_IPS32 = {0x49, 0x50, 0x53, 0x33, 0x32}; // "IPS32"

    private int mPatchType = NOT_IPS_PATCH;

    public IPS(Context context, File patch, File rom, File output) {
        super(context, patch, rom, output);
    }

    @Override
    public void apply(boolean ignoreChecksum) throws PatchException, IOException {
        apply();
    }

    public void apply() throws PatchException, IOException {
        BufferedInputStream romStream = null;
        BufferedInputStream patchStream = null;
        BufferedOutputStream outputStream = null;

        try {
            romStream = new BufferedInputStream(new FileInputStream(romFile));
            patchStream = new BufferedInputStream(new FileInputStream(patchFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            long romSize = romFile.length();
            long romPos = 0;
            long outPos = 0;
            long offset;
            long size;

            if (patchFile.length() < 14) {
                throw new PatchException(context.getString(R.string.notify_error_patch_corrupted));
            }

            byte[] magic = new byte[5];
            size = patchStream.read(magic);
            if (Arrays.equals(magic, MAGIC_NUMBER_IPS)) {
                mPatchType = IPS_PATCH;
            } else if (Arrays.equals(magic, MAGIC_NUMBER_IPS32)) {
                mPatchType = IPS32_PATCH;
            } else {
                throw new PatchException(context.getString(R.string.notify_error_not_ips_patch));
            }

            while (true) {
                offset = readOffset(patchStream);
                if (offset < 0)
                    throw new PatchException(context.getString(R.string.notify_error_patch_corrupted));
                if (checkEOF(offset)) {
                    // truncate file or copy tail
                    if (romPos < romSize) {
                        offset = readOffset(patchStream);
                        if (offset != -1 && offset >= romPos) {
                            size = offset - romPos;
                        } else {
                            size = romSize - romPos;
                        }
                        Utils.copy(romStream, outputStream, size);
                    }
                    break;
                }

                if (offset <= romSize) {
                    if (outPos < offset) {
                        size = offset - outPos;
                        Utils.copy(romStream, outputStream, size);
                        romPos += size;
                        outPos += size;
                    }
                } else {
                    if (outPos < romSize) {
                        size = romSize - outPos;
                        Utils.copy(romStream, outputStream, size);
                        romPos += size;
                        outPos += size;
                    }
                    if (outPos < offset) {
                        size = offset - outPos;
                        Utils.copy(size, (byte) 0x0, outputStream);
                        outPos += size;
                    }
                }

                size = (patchStream.read() << 8) + patchStream.read();
                if (size != 0) {
                    byte[] data = new byte[(int)size];
                    patchStream.read(data);
                    outputStream.write(data);
                    outPos += size;
                } else { // RLE
                    size = (patchStream.read() << 8) + patchStream.read();
                    byte val = (byte) patchStream.read();
                    byte[] data = new byte[(int)size];
                    Arrays.fill(data, val);
                    outputStream.write(data);
                    outPos += size;
                }

                if (offset <= romSize) {
                    if (romPos + size > romSize) {
                        romPos = romSize;
                    } else {
                        byte[] buf = new byte[(int)size];
                        romStream.read(buf);
                        romPos += size;
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(romStream);
            IOUtils.closeQuietly(patchStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    private boolean checkEOF(long value) {
        switch (mPatchType) {
            case IPS_PATCH:
                return value == 0x454f46; // "EOF"
            case IPS32_PATCH:
                return value == 0x45454f46; // "EEOF"
        }
        return false;
    }

    private long readOffset(InputStream stream) throws IOException {
        long offset = 0;
        int numBytes;
        switch (mPatchType) {
            case IPS_PATCH:
                numBytes = 3;
                break;
            case IPS32_PATCH:
                numBytes = 4;
                break;
            default:
                throw new IOException("Internal IPS error");
        }

        while (numBytes-- != 0) {
            int b = stream.read();
            if (b == -1)
                return -1;
            offset = (offset << 8) + b;
        }
        return offset;
    }
}
