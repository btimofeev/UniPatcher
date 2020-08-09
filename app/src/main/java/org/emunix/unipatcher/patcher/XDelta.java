/*
Copyright (C) 2016, 2017, 2020 Boris Timofeev

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import timber.log.Timber;

public class XDelta extends Patcher {

    private static final int NO_ERROR = 0;
    private static final int ERR_UNABLE_OPEN_PATCH = -5001;
    private static final int ERR_UNABLE_OPEN_ROM = -5002;
    private static final int ERR_UNABLE_OPEN_OUTPUT = -5003;
    private static final int ERR_WRONG_CHECKSUM = -5010;
    private static final int ERR_INTERNAL = -17710;
    private static final int ERR_INVALID_INPUT = -17712;

    public static native int xdelta3apply(String patchPath, String romPath, String outputPath, boolean ignoreChecksum);

    public XDelta(Context context, File patch, File rom, File output) {
        super(context, patch, rom, output);
    }

    @Override
    public void apply(boolean ignoreChecksum) throws PatchException, IOException {
        if (checkXDelta1(patchFile))
            throw new PatchException(context.getString(R.string.notify_error_xdelta1_unsupported));

        try {
            System.loadLibrary("xdelta3");
        } catch (UnsatisfiedLinkError e) {
            throw new PatchException(context.getString(R.string.notify_error_failed_load_lib_xdelta3));
        }

        int ret = xdelta3apply(patchFile.getPath(), romFile.getPath(), outputFile.getPath(), ignoreChecksum);
        Timber.d("XDelta3 return code: %s", ret);

        switch (ret) {
            case NO_ERROR:
                return;
            case ERR_UNABLE_OPEN_PATCH:
                throw new PatchException(context.getString(R.string.notify_error_unable_open_file)
                        .concat(" ").concat(patchFile.getName()));
            case ERR_UNABLE_OPEN_ROM:
                throw new PatchException(context.getString(R.string.notify_error_unable_open_file)
                        .concat(" ").concat(romFile.getName()));
            case ERR_UNABLE_OPEN_OUTPUT:
                throw new PatchException(context.getString(R.string.notify_error_unable_open_file)
                        .concat(" ").concat(outputFile.getName()));
            case ERR_WRONG_CHECKSUM:
                throw new PatchException(context.getString(R.string.notify_error_rom_not_compatible_with_patch));
            case ERR_INTERNAL:
                throw new PatchException(context.getString(R.string.notify_error_xdelta3_internal_error));
            case ERR_INVALID_INPUT:
                throw new PatchException(context.getString(R.string.notify_error_not_xdelta3_patch));
            default:
                throw new PatchException(context.getString(R.string.notify_error_unknown));
        }
    }

    public boolean checkXDelta1(File file) throws IOException {
        String[] MAGIC_XDELTA1 = {"%XDELTA%", "%XDZ000%", "%XDZ001%",
                "%XDZ002%", "%XDZ003%", "%XDZ004%"};

        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            byte[] magic = new byte[8];
            stream.read(magic);
            for (String xdelta1 : MAGIC_XDELTA1) {
                if (Arrays.equals(magic, xdelta1.getBytes()))
                    return true;
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return false;
    }
}
