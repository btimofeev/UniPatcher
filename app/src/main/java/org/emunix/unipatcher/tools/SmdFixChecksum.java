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
import org.emunix.unipatcher.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SmdFixChecksum {

    private Context context;
    private File smdFile = null;

    public SmdFixChecksum(Context c, File file) {
        context = c;
        smdFile = file;
    }

    private int calculateChecksum() throws RomException, IOException {
        long length = smdFile.length();
        if (length < 514) {
            throw new RomException(context.getString(R.string.notify_error_not_smd_rom));
        }

        long sum = 0;
        BufferedInputStream smdStream = null;
        try {
            smdStream = new BufferedInputStream(new FileInputStream(smdFile));

            long c = IOUtils.skip(smdStream, 512);
            if (c != 512)
                throw new IOException("Skip failed");

            int b1, b2;
            while (c < length) {
                b1 = smdStream.read();
                b2 = smdStream.read();
                if (b1 == -1 || b2 == -1)
                    throw new RomException(context.getString(R.string.notify_error_unexpected_end_of_file));

                sum += (b1 << 8) + b2;
                c += 2;
            }
        } finally {
            IOUtils.closeQuietly(smdStream);
        }

        return (int) sum & 0xffff;
    }

    public void fixChecksum() throws RomException, IOException {
        int sum = calculateChecksum();

        RandomAccessFile smdStream = null;

        try {
            smdStream = new RandomAccessFile(smdFile, "rw");
            smdStream.seek(0x18e);
            smdStream.writeByte((sum >> 8) & 0xff);
            smdStream.writeByte(sum & 0xff);
        } finally {
            IOUtils.closeQuietly(smdStream);
        }
    }
}
