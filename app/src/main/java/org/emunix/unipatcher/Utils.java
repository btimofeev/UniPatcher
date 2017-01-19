/*
Copyright (C) 2013-2017 Boris Timofeev

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

package org.emunix.unipatcher;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StatFs;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Locale;

public class Utils {
    private static final String LOG_TAG = "Utils";

    private static final int BUFFER_SIZE = 10240; // 10 Kb

    public static String getAppVersion(Context context) {
        String versionName = "N/A";
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = pinfo.versionName;
        } catch (Exception e) {
            Log.e(LOG_TAG, "App version is not available");
        }
        return versionName;
    }

    public static boolean hasStoragePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @TargetApi(18)
    public static long getFreeSpace(File file) {
        StatFs stat = new StatFs(file.getPath());
        long bytesAvailable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesAvailable = stat.getAvailableBytes();
        } else
        //noinspection deprecation
        {
            bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        }
        return bytesAvailable;
    }

    public static void copyFile(Context context, File from, File to) throws IOException {
        if (Utils.getFreeSpace(to.getParentFile()) < from.length()) {
            throw new IOException(context.getString(R.string.notify_error_not_enough_space));
        }

        try {
            FileUtils.copyFile(from, to);
        } catch (IOException e) {
            throw new IOException(context.getString(R.string.notify_error_could_not_copy_file));
        }
    }

    public static void moveFile(Context context, File from, File to) throws IOException {
        FileUtils.deleteQuietly(to);
        if (!from.renameTo(to)) {
            copyFile(context, from, to);
            FileUtils.deleteQuietly(from);
        }
    }

    public static void truncateFile(File f, long size) throws IOException {
        FileChannel channel = new FileOutputStream(f, true).getChannel();
        channel.truncate(size);
        IOUtils.closeQuietly(channel);
    }

    public static void copy(InputStream from, OutputStream to, long size) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int c;
        while (size > 0) {
            if (size < BUFFER_SIZE) {
                c = from.read(buffer, 0, (int) size);
            } else {
                c = from.read(buffer);
            }
            if (c != -1) {
                to.write(buffer, 0, c);
                size -= c;
            } else {
                copy(size, (byte) 0x0, to);
                size = 0;
            }
        }
    }

    public static void copy(long count, byte b, OutputStream to) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        Arrays.fill(buffer, b);
        while (count > 0) {
            if (count >= BUFFER_SIZE) {
                to.write(buffer);
                count -= BUFFER_SIZE;
            } else {
                to.write(buffer, 0, (int) count);
                count = 0;
            }
        }
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static boolean isPatch(File file) {
        String[] patches =
                {"ips", "ups", "bps", "aps", "ppf", "dps", "ebp", "xdelta", "xdelta3", "vcdiff"};
        String ext = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.getDefault());
        for (String patch : patches) {
            if (ext.equals(patch)) return true;
        }
        return false;
    }

    public static boolean isArchive(String path) {
        String ext = FilenameUtils.getExtension(path).toLowerCase(Locale.getDefault());
        return ext.equals("zip")
                || ext.equals("rar")
                || ext.equals("7z")
                || ext.equals("gz")
                || ext.equals("tgz");
    }
}
