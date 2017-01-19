/*
Copyright (C) 2014 Boris Timofeev

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

package org.emunix.unipatcher.tools;

import android.content.Context;

import org.apache.commons.io.IOUtils;
import org.emunix.unipatcher.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class SnesSmcHeader {

    private final static int HEADER_SIZE = 512;

    public boolean isHasSmcHeader(File romfile) {
        long romSize = romfile.length();
        return (romSize & 0x7fff) == 512;
    }

    public void deleteSnesSmcHeader(Context context, File romfile, boolean saveHeader) throws IOException, RomException {
        if (!isHasSmcHeader(romfile)) {
            throw new RomException("ROM don't have SMC header");
        }

        FileInputStream inputRom = null;
        FileOutputStream outputRom = null;
        FileOutputStream outputHeader = null;
        File tmpfile;

        try {
            tmpfile = File.createTempFile(romfile.getName(), null, romfile.getParentFile());

            inputRom = new FileInputStream(romfile);
            outputRom = new FileOutputStream(tmpfile);

            // write smc header in a file
            byte[] header = new byte[HEADER_SIZE];
            int length;
            length = inputRom.read(header);
            if (saveHeader) {
                File headerfile = new File(romfile.getPath() + ".smc_header");
                outputHeader = new FileOutputStream(headerfile);
                outputHeader.write(header, 0, length);
            }

            // write headerless rom in tmp file
            byte[] buffer = new byte[32768];
            while ((length = inputRom.read(buffer)) > 0) {
                outputRom.write(buffer, 0, length);
            }
        } finally {
            IOUtils.closeQuietly(inputRom);
            IOUtils.closeQuietly(outputRom);
            IOUtils.closeQuietly(outputHeader);
        }

        Utils.moveFile(context, tmpfile, romfile);
    }

    public void addSnesSmcHeader(Context context, File romfile, File headerfile) throws IOException, RomException {
        if (isHasSmcHeader(romfile)) {
            throw new RomException();
        }

        FileInputStream inputRom = null;
        FileInputStream inputHeader = null;
        FileOutputStream outputRom = null;
        File tmpfile;

        try {
            tmpfile = File.createTempFile(romfile.getName(), null, romfile.getParentFile());

            inputRom = new FileInputStream(romfile);
            outputRom = new FileOutputStream(tmpfile);

            // write header to tmp file
            byte[] header = new byte[HEADER_SIZE];
            int length;
            if (headerfile == null) {
                Arrays.fill(header, (byte) 0);
                length = HEADER_SIZE;
            } else {
                inputHeader = new FileInputStream(headerfile);
                length = inputHeader.read(header);
            }
            outputRom.write(header, 0, length);

            // write headerless rom in tmp file
            byte[] buffer = new byte[32768];
            while ((length = inputRom.read(buffer)) > 0) {
                outputRom.write(buffer, 0, length);
            }
        } finally {
            IOUtils.closeQuietly(inputRom);
            IOUtils.closeQuietly(inputHeader);
            IOUtils.closeQuietly(outputRom);
        }

        Utils.moveFile(context, tmpfile, romfile);
    }

    public void addSnesSmcHeader(Context context, File romfile) throws IOException, RomException {
        addSnesSmcHeader(context, romfile, null);
    }
}