/*
Copyright (C) 2013, 2016, 2021 Boris Timofeev

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
import java.util.Arrays;
import java.util.zip.CRC32;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.helpers.ResourceProvider;
import org.emunix.unipatcher.utils.UFileUtils;

public class UPS extends Patcher {

    private static final byte[] MAGIC_NUMBER = {0x55, 0x50, 0x53, 0x31}; // "UPS1"

    public UPS(File patch, File rom, File output, ResourceProvider resourceProvider, UFileUtils fileUtils) {
        super(patch, rom, output, resourceProvider, fileUtils);
    }

    @Override
    public void apply(boolean ignoreChecksum) throws PatchException, IOException {

        if (patchFile.length() < 18) {
            throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
        }

        BufferedInputStream patchStream = null;
        BufferedInputStream romStream = null;
        BufferedOutputStream outputStream = null;
        UpsCrc upsCrc;
        try {
            if (!checkMagic(patchFile)) {
                throw new PatchException(resourceProvider.getString(R.string.notify_error_not_ups_patch));
            }

            upsCrc = readUpsCrc(patchFile, resourceProvider);
            if (upsCrc.getPatchFileCRC() != upsCrc.getRealPatchCRC()) {
                throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
            }

            patchStream = new BufferedInputStream(new FileInputStream(patchFile));
            long patchPos = 0;
            // skip magic
            for (int i = 0; i < 4; i++) {
                patchStream.read();
            }
            patchPos += 4;

            // decode rom and output size
            Pair p;
            p = decode(patchStream);
            long xSize = p.getValue();
            patchPos += p.getSize();
            p = decode(patchStream);
            long ySize = p.getValue();
            patchPos += p.getSize();

            long realRomCrc = fileUtils.checksumCRC32(romFile);

            if (romFile.length() == xSize && realRomCrc == upsCrc.getInputFileCRC()) {
                // xSize, ySize, inCRC, outCRC not change
            } else if (romFile.length() == ySize && realRomCrc == upsCrc.getOutputFileCRC()) {
                // swap(xSize, ySize) and swap(inCRC, outCRC)
                long tmp = xSize;
                xSize = ySize;
                ySize = tmp;
                upsCrc.swapInOut();
            } else {
                if (!ignoreChecksum) {
                    throw new IOException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch));
                }
            }

            romStream = new BufferedInputStream(new FileInputStream(romFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
            long outPos = 0;

            int x, y;
            long offset = 0;
            while (patchPos < patchFile.length() - 12) {
                p = decode(patchStream);
                offset += p.getValue();
                patchPos += p.getSize();
                if (offset > ySize) {
                    continue;
                }
                fileUtils.copy(romStream, outputStream, offset - outPos);
                outPos += offset - outPos;
                for (long i = offset; i < ySize; i++) {
                    x = patchStream.read();
                    patchPos++;
                    offset++;
                    if (x == 0x00) {
                        break; // chunk terminating byte - 0x00
                    }
                    y = i < xSize ? romStream.read() : 0x00;
                    outputStream.write(x ^ y);
                    outPos++;
                }
            }
            // write rom tail and trim
            fileUtils.copy(romStream, outputStream, ySize - outPos);

        } finally {
            fileUtils.closeQuietly(patchStream);
            fileUtils.closeQuietly(romStream);
            fileUtils.closeQuietly(outputStream);
        }

        if (!ignoreChecksum) {
            long realOutCrc = fileUtils.checksumCRC32(outputFile);
            if (realOutCrc != upsCrc.getOutputFileCRC()) {
                throw new PatchException(resourceProvider.getString(R.string.notify_error_wrong_checksum_after_patching));
            }
        }
    }

    // decode pointer
    private Pair decode(BufferedInputStream stream) throws PatchException, IOException {
        long offset = 0;
        long size = 0;
        int shift = 1;
        int x;
        while (true) {
            x = stream.read();
            if (x == -1) {
                throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
            }
            size++;
            offset += (x & 0x7fL) * shift;
            if ((x & 0x80) != 0) {
                break;
            }
            shift <<= 7;
            offset += shift;
        }
        return new Pair(offset, size);
    }

    public static boolean checkMagic(File f) throws IOException {
        try (FileInputStream stream = new FileInputStream(f)) {
            byte[] buffer = new byte[4];
            stream.read(buffer);
            return Arrays.equals(buffer, MAGIC_NUMBER);
        }
    }

    public static UpsCrc readUpsCrc(File f, ResourceProvider resourceProvider) throws PatchException, IOException {
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(f))) {
            CRC32 crc = new CRC32();
            int x;
            for (long i = f.length() - 12; i != 0; i--) {
                x = stream.read();
                if (x == -1) {
                    throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
                }
                crc.update(x);
            }

            long inputCrc = 0;
            for (int i = 0; i < 4; i++) {
                x = stream.read();
                if (x == -1) {
                    throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
                }
                crc.update(x);
                inputCrc += ((long) x) << (i * 8);
            }

            long outputCrc = 0;
            for (int i = 0; i < 4; i++) {
                x = stream.read();
                if (x == -1) {
                    throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
                }
                crc.update(x);
                outputCrc += ((long) x) << (i * 8);
            }

            long realPatchCrc = crc.getValue();
            long patchCrc = readLong(stream);
            if (patchCrc == -1) {
                throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
            }
            return new UpsCrc(inputCrc, outputCrc, patchCrc, realPatchCrc);
        }
    }

    private static long readLong(BufferedInputStream stream) throws IOException {
        long result = 0;
        int x;
        for (int i = 0; i < 4; i++) {
            x = stream.read();
            if (x == -1) {
                return -1;
            }
            result += ((long) x) << (i * 8);
        }
        return result;
    }

    public static class UpsCrc {

        private long inputFileCRC;
        private long outputFileCRC;
        private long patchFileCRC;
        private long realPatchCRC;

        public UpsCrc(long inputFileCRC, long outputFileCRC, long patchFileCRC, long realPatchCRC) {

            this.inputFileCRC = inputFileCRC;
            this.outputFileCRC = outputFileCRC;
            this.patchFileCRC = patchFileCRC;
            this.realPatchCRC = realPatchCRC;
        }

        public long getInputFileCRC() {
            return inputFileCRC;
        }

        public long getOutputFileCRC() {
            return outputFileCRC;
        }

        public long getPatchFileCRC() {
            return patchFileCRC;
        }

        public long getRealPatchCRC() {
            return realPatchCRC;
        }

        public void swapInOut() {
            long tmp;
            tmp = inputFileCRC;
            inputFileCRC = outputFileCRC;
            outputFileCRC = tmp;
        }
    }

    final class Pair {

        private final long value;
        private final long size;

        public Pair(long value, long size) {
            this.value = value;
            this.size = size;
        }

        public long getValue() {
            return value;
        }

        public long getSize() {
            return size;
        }
    }
}
