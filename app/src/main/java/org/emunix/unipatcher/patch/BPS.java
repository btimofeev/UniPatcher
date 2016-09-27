/*
Copyright (C) 2016 Boris Timofeev

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.emunix.unipatcher.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.zip.CRC32;

public class BPS extends Patch {

    private static final byte[] MAGIC_NUMBER = {0x42, 0x50, 0x53, 0x31}; // "BPS1"
    private static final byte SOURCE_READ = 0b00;
    private static final byte TARGET_READ = 0b01;
    private static final byte SOURCE_COPY = 0b10;
    private static final byte TARGET_COPY = 0b11;

    private ByteBuffer buffer = ByteBuffer.allocate(10485760); // 10mb

    public BPS(Context context, File patch, File rom, File output) {
        super(context, patch, rom, output);
    }

    @Override
    public void apply() throws PatchException, IOException {

        if (patchFile.length() < 19) {
            throw new PatchException(context.getString(R.string.notify_error_patch_corrupted));
        }

        FileChannel patch = null;
        FileChannel rom = null;
        FileChannel output = null;
        BpsCrc bpsCrc;
        try {
            if (!checkMagic(patchFile))
                throw new PatchException(context.getString(R.string.notify_error_not_bps_patch));

            bpsCrc = readBpsCrc(context, patchFile);
            if (bpsCrc.getPatchFileCRC() != bpsCrc.getRealPatchCRC())
                throw new PatchException(context.getString(R.string.notify_error_patch_corrupted));

            long realRomCrc = FileUtils.checksumCRC32(romFile);
            if (realRomCrc != bpsCrc.getInputFileCRC()) {
                throw new PatchException(context.getString(R.string.notify_error_rom_not_compatible_with_patch));
            }

            patch = new RandomAccessFile(patchFile, "r").getChannel();
            patch.position(4); // skip magic

            // decode rom size
            long romSize = decode(patch);
            rom = new RandomAccessFile(romFile, "r").getChannel();

            // decode output size
            long outputSize = decode(patch);
            output = new RandomAccessFile(outputFile, "rw").getChannel();

            // decode metadata size and skip
            int metadataSize = (int) decode(patch);
            for (int i = 0; i < metadataSize; i++) {
                patch.position(patch.position() + metadataSize);
            }

            int romRelOffset = 0;
            int outRelOffset = 0;
            long offset;
            long length;
            byte mode;

            while (patch.position() < patchFile.length() - 12) {
                length = decode(patch);
                mode = (byte) (length & 0b11);
                length = (length >> 2) + 1;

                switch (mode) {
                    case SOURCE_READ:
                        copy(rom, output.position(), length, output);
                        break;
                    case TARGET_READ:
                        copy(patch, patch.position(), length, output);
                        patch.position(patch.position() + length);
                        break;
                    case SOURCE_COPY:
                    case TARGET_COPY:
                        offset = decode(patch);
                        byte negative = (byte) (offset & 1);
                        offset >>= 1;
                        if (negative == 1) offset = -offset;

                        if (mode == SOURCE_COPY) {
                            romRelOffset += offset;
                            copy(rom, romRelOffset, length, output);
                            romRelOffset += length;
                        } else {
                            outRelOffset += offset;
                            copyTarget(output, outRelOffset, length);
                            outRelOffset += length;
                        }
                }
            }
        } finally {
            IOUtils.closeQuietly(patch);
            IOUtils.closeQuietly(rom);
            IOUtils.closeQuietly(output);
        }

        long realOutCrc = FileUtils.checksumCRC32(outputFile);
        if (realOutCrc != bpsCrc.getOutputFileCRC())
            throw new PatchException(context.getString(R.string.notify_error_wrong_checksum_after_patching));
    }

    // decode pointer
    private long decode(FileChannel fc) throws IOException {
        buffer.clear();
        buffer.limit(1);
        long offset = 0;
        int shift = 1;
        int c, x;
        while (true) {
            c = fc.read(buffer);
            if (c < 1) throw new IOException("read < 1 byte");
            x = buffer.get(0);
            offset += (x & 0x7fL) * shift;
            if ((x & 0x80) != 0) break;
            shift <<= 7;
            offset += shift;
            buffer.flip();
        }
        buffer.clear();
        return offset;
    }

    private void copy(FileChannel from, long pos, long size, FileChannel to) throws IOException {
        buffer.clear();
        int c1, c2;
        while (size > 0) {
            if (size < buffer.capacity())
                buffer.limit((int) size);
            c1 = from.read(buffer, pos);
            buffer.flip();
            c2 = to.write(buffer);
            buffer.clear();
            if (c1 != c2)
                throw new IOException("Read and write a different number of bytes");
            pos += c1;
            size -= c1;
        }
    }

    private void copyTarget(FileChannel fc, int offset, long size) throws IOException {
        long bufSize = fc.position() + size - offset;
        buffer.clear();
        if (bufSize <= buffer.capacity()) {
            buffer.limit((int) bufSize);
            fc.read(buffer, offset);
            buffer.position((int) (fc.position() - offset));
            int bufferOffset = 0;
            while (size-- != 0) {
                buffer.put(buffer.get(bufferOffset++));
            }
            buffer.position((int) (fc.position() - offset));
            fc.write(buffer);
        } else { // very strange patch
            buffer.limit(1);
            while (size-- != 0) {
                fc.read(buffer, offset);
                buffer.flip();
                fc.write(buffer);
                buffer.flip();
            }
        }
        buffer.clear();
    }

    public static boolean checkMagic(File f) throws IOException {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(f);
            byte[] buffer = new byte[4];
            stream.read(buffer);
            return Arrays.equals(buffer, MAGIC_NUMBER);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    public static BpsCrc readBpsCrc(Context context, File f) throws PatchException, IOException {
        BufferedInputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(f));
            CRC32 crc = new CRC32();
            int x;
            for (long i = f.length() - 12; i != 0; i--) {
                x = stream.read();
                if (x == -1)
                    throw new PatchException(context.getString(R.string.notify_error_patch_corrupted));
                crc.update(x);
            }

            long inputCrc = 0;
            for (int i = 0; i < 4; i++) {
                x = stream.read();
                if (x == -1)
                    throw new PatchException(context.getString(R.string.notify_error_patch_corrupted));
                crc.update(x);
                inputCrc += ((long) x) << (i * 8);
            }

            long outputCrc = 0;
            for (int i = 0; i < 4; i++) {
                x = stream.read();
                if (x == -1)
                    throw new PatchException(context.getString(R.string.notify_error_patch_corrupted));
                crc.update(x);
                outputCrc += ((long) x) << (i * 8);
            }

            long realPatchCrc = crc.getValue();
            long patchCrc = readLong(stream);
            if (patchCrc == -1)
                throw new PatchException(context.getString(R.string.notify_error_patch_corrupted));
            return new BpsCrc(inputCrc, outputCrc, patchCrc, realPatchCrc);
        } finally {
           IOUtils.closeQuietly(stream);
        }
    }

    private static long readLong(BufferedInputStream stream) throws IOException {
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

    public static class BpsCrc {
        private long inputFileCRC;
        private long outputFileCRC;
        private long patchFileCRC;
        private long realPatchCRC;

        public BpsCrc(long inputFileCRC, long outputFileCRC, long patchFileCRC, long realPatchCRC) {

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

    }

}
