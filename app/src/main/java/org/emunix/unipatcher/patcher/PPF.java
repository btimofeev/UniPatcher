/*
Copyright (C) 2013, 2021 Boris Timofeev

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
import java.io.RandomAccessFile;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.utils.UFileUtils;
import org.emunix.unipatcher.helpers.ResourceProvider;

public class PPF extends Patcher {

    private static final byte[] MAGIC_NUMBER = {0x50, 0x50, 0x46}; // "PPF" without version

    private RandomAccessFile patchStream;
    private RandomAccessFile outputStream;

    public PPF(File patch, File rom, File output, ResourceProvider resourceProvider, UFileUtils fileUtils) {
        super(patch, rom, output, resourceProvider, fileUtils);
    }

    /**
     * Check what PPF version we have.
     *
     * @param file PPF patch
     * @return PPF patch version or 0 if the file is not a PPF patch
     * @throws IOException
     */
    private int getPPFVersion(File file) throws IOException {
        FileInputStream stream = null;
        int version = 0;
        try {
            stream = new FileInputStream(file);
            byte[] buffer = new byte[3];
            stream.read(buffer);
            if (Arrays.equals(buffer, MAGIC_NUMBER)) {
                int b = stream.read();
                if (b == 0x31) version = 1;
                else if (b == 0x32) version = 2;
                else if (b == 0x33) version = 3;
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return version;
    }

    @Override
    public void apply(boolean ignoreChecksum) throws PatchException, IOException {
        if (patchFile.length() < 61) {
            throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
        }

        fileUtils.copyFile(romFile, outputFile);

        switch (getPPFVersion(patchFile)) {
            case 1:
                applyPPF1();
                break;
            case 2:
                applyPPF2(ignoreChecksum);
                break;
            case 3:
                applyPPF3(ignoreChecksum);
                break;
            default:
                throw new PatchException(resourceProvider.getString(R.string.notify_error_not_ppf_patch));
        }
    }

    private void applyPPF1() throws IOException {
        try {
            patchStream = new RandomAccessFile(patchFile, "r");
            outputStream = new RandomAccessFile(outputFile, "rw");

            long dataEnd = patchFile.length();
            int chunkSize;
            byte[] chunkData = new byte[256];
            long offset;

            patchStream.seek(56);
            while (patchStream.getFilePointer() < dataEnd) {
                offset = readLittleEndianInt(patchStream);
                chunkSize = patchStream.readUnsignedByte();
                patchStream.read(chunkData, 0, chunkSize);
                outputStream.seek(offset);
                outputStream.write(chunkData, 0, chunkSize);
            }
        } finally {
            IOUtils.closeQuietly(patchStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    private void applyPPF2(boolean ignoreChecksum) throws IOException, PatchException {
        try {
            patchStream = new RandomAccessFile(patchFile, "r");

            // Check size of ROM
            patchStream.seek(56);
            long romSize = readLittleEndianInt(patchStream);
            if (!ignoreChecksum) {
                if (romSize != romFile.length()) {
                    throw new PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch));
                }
            }

            outputStream = new RandomAccessFile(outputFile, "rw");

            // Check binary block
            byte[] patchBinaryBlock = new byte[1024];
            byte[] romBinaryBlock = new byte[1024];
            outputStream.seek(0x9320);
            patchStream.read(patchBinaryBlock, 0, 1024);
            outputStream.read(romBinaryBlock, 0, 1024);
            if (!ignoreChecksum) {
                if (!Arrays.equals(patchBinaryBlock, romBinaryBlock))
                    throw new PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch));
            }

            // Calculate end of patch data
            long dataEnd = patchFile.length();
            int sizeFileId = getSizeFileId(patchStream, 2);
            if (sizeFileId > 0) {
                dataEnd -= (18 + sizeFileId + 16 + 4);
            }

            // Apply patch
            int chunkSize;
            byte[] chunkData = new byte[256];
            long offset;

            patchStream.seek(1084);
            while (patchStream.getFilePointer() < dataEnd) {
                offset = readLittleEndianInt(patchStream);
                chunkSize = patchStream.readUnsignedByte();
                patchStream.read(chunkData, 0, chunkSize);
                outputStream.seek(offset);
                outputStream.write(chunkData, 0, chunkSize);
            }
        } finally {
            IOUtils.closeQuietly(patchStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    private void applyPPF3(boolean ignoreChecksum) throws IOException, PatchException {
        try {
            patchStream = new RandomAccessFile(patchFile, "r");
            outputStream = new RandomAccessFile(outputFile, "rw");

            patchStream.seek(56);
            byte imagetype = patchStream.readByte();
            byte blockcheck = patchStream.readByte();
            byte undo = patchStream.readByte();

            // Check binary block
            if (blockcheck == 0x01) {
                byte[] patchBinaryBlock = new byte[1024];
                byte[] romBinaryBlock = new byte[1024];
                patchStream.seek(60);
                if (imagetype == 0x01) {
                    outputStream.seek(0x80A0);
                } else {
                    outputStream.seek(0x9320);
                }
                patchStream.read(patchBinaryBlock, 0, 1024);
                outputStream.read(romBinaryBlock, 0, 1024);
                if (!ignoreChecksum) {
                    if (!Arrays.equals(patchBinaryBlock, romBinaryBlock))
                        throw new PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch));
                }
            }

            // Calculate end of patch data
            long dataEnd = patchFile.length();
            int sizeFileId = getSizeFileId(patchStream, 3);
            if (sizeFileId > 0) {
                dataEnd -= (18 + sizeFileId + 16 + 2);
            }

            // Seek start address of patch data
            if (blockcheck == 0x01) {
                patchStream.seek(1084);
            } else {
                patchStream.seek(60);
            }

            // Apply patch
            int chunkSize;
            byte[] chunkData = new byte[512];
            long offset;

            while (patchStream.getFilePointer() < dataEnd) {
                offset = readLittleEndianLong(patchStream);
                //Log.d(LOG_TAG, String.valueOf(patchStream.getFilePointer()) + ' ' + String.valueOf(offset));
                chunkSize = patchStream.readUnsignedByte();
                patchStream.read(chunkData, 0, chunkSize);
                if (undo == 0x01) patchStream.seek(patchStream.getFilePointer() + chunkSize);
                outputStream.seek(offset);
                outputStream.write(chunkData, 0, chunkSize);
            }
        } finally {
            IOUtils.closeQuietly(patchStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    private long readLittleEndianLong(RandomAccessFile stream) throws IOException {
        byte[] b = new byte[8];
        stream.read(b);
        return ((long) (b[7] & 0xff) << 56) + ((long) (b[6] & 0xff) << 48) +
                ((long) (b[5] & 0xff) << 40) + ((long) (b[4] & 0xff) << 32) +
                ((long) (b[3] & 0xff) << 24) + ((long) (b[2] & 0xff) << 16) +
                ((long) (b[1] & 0xff) << 8) + ((long) b[0] & 0xff);
    }

    private int readLittleEndianInt(RandomAccessFile stream) throws IOException {
        byte[] b = new byte[4];
        stream.read(b);
        return ((b[3] & 0xff) << 24) + ((b[2] & 0xff) << 16) +
                ((b[1] & 0xff) << 8) + (b[0] & 0xff);
    }

    /**
     * Returns size of FileID
     *
     * @param stream     stream of PPF file
     * @param ppfVersion version of PPF patch
     * @return size of FileID or 0
     */
    private int getSizeFileId(RandomAccessFile stream, int ppfVersion) throws IOException {
        final byte[] magic = {0x2E, 0x44, 0x49, 0x5A}; // ".DIZ"
        byte[] buffer = new byte[4];
        int result;

        if (ppfVersion == 2) {
            stream.seek(stream.length() - 4 - 4);
        } else {
            stream.seek(stream.length() - 2 - 4);
        }

        stream.read(buffer, 0, 4);
        if (!Arrays.equals(magic, buffer)) {
            return 0;
        }

        if (ppfVersion == 2) {
            result = readLittleEndianInt(stream);
        } else {
            result = stream.readUnsignedByte() + (stream.readUnsignedByte() << 8);
        }

        if (result > 3072) result = 3072;
        return result;
    }

}
