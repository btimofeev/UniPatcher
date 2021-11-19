/*
This file based on source code of EBPatcher by Marc Gagné (https://github.com/Lyrositor/EBPatcher)

Copyright (C) 2016, 2020, 2021 Boris Timofeev

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

import androidx.annotation.NonNull;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.utils.UFileUtils;
import org.emunix.unipatcher.helpers.ResourceProvider;
import org.emunix.unipatcher.tools.RomException;
import org.emunix.unipatcher.tools.SnesSmcHeader;
import org.emunix.unipatcher.utils.ExtensionsKt;

public class EBP extends Patcher {

    private static final byte[] MAGIC_NUMBER = {0x50, 0x41, 0x54, 0x43, 0x48}; // "PATCH"
    private static final byte[] EARTH_BOUND = {0x45, 0x41, 0x52, 0x54, 0x48, 0x20, 0x42, 0x4f, 0x55, 0x4e, 0x44};
    private static final String EB_CLEAN_MD5 = "a864b2e5c141d2dec1c4cbed75a42a85";
    private static final int EB_CLEAN_ROM_SIZE = 0x300000;
    private static final HashMap<String, String> EB_WRONG_MD5;

    static {
        EB_WRONG_MD5 = new HashMap<>();
        EB_WRONG_MD5.put("8c28ce81c7d359cf9ccaa00d41f8ad33", "patch/ebp/wrong1.ips");
        EB_WRONG_MD5.put("b2dcafd3252cc4697bf4b89ea3358cd5", "patch/ebp/wrong2.ips");
        EB_WRONG_MD5.put("0b8c04fc0182e380ff0e3fe8fdd3b183", "patch/ebp/wrong3.ips");
        EB_WRONG_MD5.put("2225f8a979296b7dcccdda17b6a4f575", "patch/ebp/wrong4.ips");
        EB_WRONG_MD5.put("eb83b9b6ea5692cefe06e54ea3ec9394", "patch/ebp/wrong5.ips");
        EB_WRONG_MD5.put("cc9fa297e7bf9af21f7f179e657f1aa1", "patch/ebp/wrong6.ips");
    }

    public EBP(File patch, File rom, File output, ResourceProvider resourceProvider, UFileUtils fileUtils) {
        super(patch, rom, output, resourceProvider, fileUtils);
    }

    @Override
    public void apply(boolean ignoreChecksum) throws PatchException, IOException {
        File cleanRom = File.createTempFile("rom", null, resourceProvider.getTempDir());
        File ipsPatch = File.createTempFile("patch", null, resourceProvider.getTempDir());
        try {
            fileUtils.copyFile(romFile, cleanRom);
            prepareCleanRom(cleanRom, ignoreChecksum);

            EBPtoIPS(patchFile, ipsPatch);

            IPS ips = new IPS(ipsPatch, cleanRom, outputFile, resourceProvider, fileUtils);
            ips.apply();
        } finally {
            fileUtils.delete(ipsPatch);
            fileUtils.delete(cleanRom);
        }
    }

    private void prepareCleanRom(File file, boolean ignoreChecksum) throws IOException, PatchException {
        // delete smc header
        try {
            new SnesSmcHeader().deleteSnesSmcHeader(romFile, file, resourceProvider, fileUtils);
        } catch (RomException e) {
            // no header
        }

        // check rom size and remove unused expanded space
        if (!ignoreChecksum) {
            if (file.length() < EB_CLEAN_ROM_SIZE) {
                throw new PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch));
            }
        }
        if (file.length() > EB_CLEAN_ROM_SIZE && checkExpanded(file)) {
            removeExpanded(file);
        }

        // try to fix the ROM if it's incorrect
        if (!checkMD5(file)) {
            repairRom(file);
        }

        // if we couldn't fix the ROM, try to remove a 0xff byte at the end.
        if (!checkMD5(file)) {
            int length = (int) file.length();
            byte[] buffer = new byte[length];
            FileInputStream in = new FileInputStream(file);
            int count = in.read(buffer);
            in.close();
            if (count != file.length()) {
                throw new IOException("Unable read file");
            }
            if (buffer[length - 1] == 0xff) {
                buffer[length - 1] = 0;
            }

            if (checkMD5(buffer)) {
                RandomAccessFile f = new RandomAccessFile(file, "rw");
                f.seek(length - 1);
                f.write(0);
                f.close();
            }
        }

        if (!checkMD5(file) || !checkEarthBound(file)) {
            throw new PatchException(resourceProvider.getString(R.string.notify_error_rom_not_compatible_with_patch));
        }
    }

    private boolean checkExpanded(File file) throws IOException {
        byte[] byteArray = new byte[EB_CLEAN_ROM_SIZE];
        FileInputStream f = new FileInputStream(file);
        int count = f.read(byteArray);
        fileUtils.closeQuietly(f);
        if (count < EB_CLEAN_ROM_SIZE) {
            throw new IOException("Unable to read 0x300000 bytes from ROM");
        }
        // ExHiROM expanded ROMs have two bytes different from LoROM.
        byteArray[0xffd5] = 0x31;
        byteArray[0xffd7] = 0x0c;

        // If the normal area is unmodified, then the expanded area is unused and can be deleted.
        return checkMD5(byteArray);
    }

    private void removeExpanded(File file) throws IOException {
        if (file.length() > 0x400000) {
            RandomAccessFile f = new RandomAccessFile(file, "rw");
            f.seek(0xffd5);
            f.write(0x31);
            f.seek(0xffd7);
            f.write(0x0c);
            f.close();
        }
        FileChannel fc = new FileOutputStream(file, true).getChannel();
        fc.truncate(EB_CLEAN_ROM_SIZE);
        fc.close();
    }

    private void repairRom(File file) throws IOException, PatchException {
        String md5 = calculateMD5(file);
        if (EB_WRONG_MD5.containsKey(md5)) {

            // copy patch from assets
            InputStream in = resourceProvider.getAsset(EB_WRONG_MD5.get(md5));
            File patch = File.createTempFile("patch", null, resourceProvider.getTempDir());
            fileUtils.copyToFile(in, patch);
            fileUtils.closeQuietly(in);

            // fix rom
            File tmpFile = File.createTempFile("rom", null, resourceProvider.getTempDir());
            fileUtils.copyFile(file, tmpFile);
            IPS ips = new IPS(patch, tmpFile, file, resourceProvider, fileUtils);
            ips.apply();

            fileUtils.delete(tmpFile);
            fileUtils.delete(patch);
        }
    }

    private boolean checkMD5(byte[] array) throws IOException {
        try {
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            md5Digest.update(array);
            String md5 = ExtensionsKt.bytesToHexString(md5Digest.digest());
            return md5.equals(EB_CLEAN_MD5);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean checkMD5(File file) throws IOException {
        String md5 = calculateMD5(file);
        return md5.equals(EB_CLEAN_MD5);
    }

    @NonNull
    private String calculateMD5(File file) throws IOException {
        FileInputStream f = new FileInputStream(file);
        try {
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            byte[] byteArray = new byte[32768];
            int count;
            while ((count = f.read(byteArray)) != -1) {
                md5Digest.update(byteArray, 0, count);
            }
            return ExtensionsKt.bytesToHexString(md5Digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e.getMessage());
        } finally {
            fileUtils.closeQuietly(f);
        }
    }

    private boolean checkEarthBound(File file) throws IOException {
        byte[] buffer = new byte[11];
        RandomAccessFile f = new RandomAccessFile(file, "r");
        f.seek(0xffc0);
        f.read(buffer);
        f.close();
        return Arrays.equals(EARTH_BOUND, buffer);
    }

    private void EBPtoIPS(File ebpFile, File ipsFile) throws IOException, PatchException {
        BufferedInputStream ebp = null;
        BufferedOutputStream ips = null;
        try {
            ebp = new BufferedInputStream(new FileInputStream(ebpFile));
            ips = new BufferedOutputStream(new FileOutputStream(ipsFile));

            int size;
            byte[] buffer = new byte[65536];

            if (ebpFile.length() < 14) {
                throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
            }

            // check magic string
            byte[] magic = new byte[5];
            size = ebp.read(magic);
            if (size != 5 || !Arrays.equals(magic, MAGIC_NUMBER)) {
                throw new PatchException(resourceProvider.getString(R.string.notify_error_not_ebp_patch));
            }

            ips.write(magic);

            while (true) {
                size = ebp.read(buffer, 0, 3);
                if (size < 3) {
                    throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
                }
                ips.write(buffer, 0, 3);
                if (buffer[0] == 0x45 && buffer[1] == 0x4f && buffer[2] == 0x46) // EOF
                {
                    break;
                }
                size = ebp.read(buffer, 0, 2);
                if (size < 2) {
                    throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
                }
                ips.write(buffer, 0, 2);
                size = (((int) buffer[0] & 0xff) << 8) + ((int) buffer[1] & 0xff);
                if (size != 0) {
                    int c = ebp.read(buffer, 0, size);
                    if (c < size) {
                        throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
                    }
                    ips.write(buffer, 0, size);
                } else {
                    size = ebp.read(buffer, 0, 3);
                    if (size < 3) {
                        throw new PatchException(resourceProvider.getString(R.string.notify_error_patch_corrupted));
                    }
                    ips.write(buffer, 0, 3);
                }
            }
        } finally {
            fileUtils.closeQuietly(ips);
            fileUtils.closeQuietly(ebp);
        }
    }
}
