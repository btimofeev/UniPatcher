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

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.emunix.unipatcher.patch.APS;
import org.emunix.unipatcher.patch.BPS;
import org.emunix.unipatcher.patch.DPS;
import org.emunix.unipatcher.patch.EBP;
import org.emunix.unipatcher.patch.IPS;
import org.emunix.unipatcher.patch.PPF;
import org.emunix.unipatcher.patch.Patch;
import org.emunix.unipatcher.patch.PatchException;
import org.emunix.unipatcher.patch.UPS;
import org.emunix.unipatcher.patch.XDelta;
import org.emunix.unipatcher.tools.RomException;
import org.emunix.unipatcher.tools.SmdFixChecksum;
import org.emunix.unipatcher.tools.SnesSmcHeader;
import org.emunix.unipatcher.ui.activity.MainActivity;
import org.emunix.unipatcher.ui.notify.Notify;
import org.emunix.unipatcher.ui.notify.PatchingNotify;
import org.emunix.unipatcher.ui.notify.SmdFixChecksumNotify;
import org.emunix.unipatcher.ui.notify.SnesAddSmcHeaderNotify;
import org.emunix.unipatcher.ui.notify.SnesDeleteSmcHeaderNotify;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class WorkerService extends IntentService {

    public WorkerService() {
        super("WorkerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // if user deny write storage permission
        if (!Utils.hasStoragePermission(this)) {
            showErrorNotification(getString(R.string.permissions_storage_error_notify_access_denied));
            return;
        }

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UniPatcher");
        wakeLock.acquire();

        try {
            int action = intent.getIntExtra("action", 0);
            switch (action) {
                case Globals.ACTION_PATCHING:
                    actionPatching(intent);
                    break;
                case Globals.ACTION_SMD_FIX_CHECKSUM:
                    actionSmdFixChecksum(intent);
                    break;
                case Globals.ACTION_SNES_ADD_SMC_HEADER:
                    actionSnesAddSmcHeader(intent);
                    break;
                case Globals.ACTION_SNES_DELETE_SMC_HEADER:
                    actionSnesDeleteSmcHeader(intent);
                    break;
            }
        } finally {
            wakeLock.release();
        }
    }

    private void actionPatching(Intent intent) {
        String errorMsg = null;
        File romFile = new File(intent.getStringExtra("romPath"));
        File patchFile = new File(intent.getStringExtra("patchPath"));
        File outputFile = new File(intent.getStringExtra("outputPath"));
        Patch patcher = null;

        if(!fileExists(patchFile) || !fileExists(romFile))
            return;

        // create output dir
        try {
            if (!outputFile.getParentFile().exists()) {
                FileUtils.forceMkdirParent(outputFile);
            }
        } catch (IOException | SecurityException e) {
            String text = getString(R.string.notify_error_unable_to_create_directory, outputFile.getParent());
            showErrorNotification(text);
            return;
        }

        // check access to output dir
        try {
            if (!outputFile.getParentFile().canWrite()){
                String text = getString(R.string.notify_error_unable_to_write_to_directory, outputFile.getParent());
                showErrorNotification(text);
                return;
            }
        } catch (SecurityException e) {
            String text = getString(R.string.notify_error_unable_to_write_to_directory, outputFile.getParent());
            showErrorNotification(text);
            return;
        }

        String ext = FilenameUtils.getExtension(patchFile.getName()).toLowerCase(Locale.getDefault());
        if ("ips".equals(ext))
            patcher = new IPS(this, patchFile, romFile, outputFile);
        else if ("ups".equals(ext))
            patcher = new UPS(this, patchFile, romFile, outputFile);
        else if ("bps".equals(ext))
            patcher = new BPS(this, patchFile, romFile, outputFile);
        else if ("ppf".equals(ext))
            patcher = new PPF(this, patchFile, romFile, outputFile);
        else if ("aps".equals(ext))
            patcher = new APS(this, patchFile, romFile, outputFile);
        else if ("ebp".equals(ext))
            patcher = new EBP(this, patchFile, romFile, outputFile);
        else if ("dps".equals(ext))
            patcher = new DPS(this, patchFile, romFile, outputFile);
        else if ("xdelta".equals(ext) || "xdelta3".equals(ext) || "vcdiff".equals(ext))
            patcher = new XDelta(this, patchFile, romFile, outputFile);
        else
            errorMsg = getString(R.string.notify_error_unknown_patch_format);

        Notify notify = new PatchingNotify(this, outputFile.getName());

        if (errorMsg != null) {
            notify.showResult(errorMsg);
            return;
        }

        startForeground(notify.getID(), notify.getNotifyBuilder().build());

        try {
            if ("ppf".equals(ext))
                Utils.copyFile(this, romFile, outputFile);
            patcher.apply();
        } catch (PatchException | IOException e) {
            if (Utils.getFreeSpace(outputFile.getParentFile()) == 0) {
                errorMsg = getString(R.string.notify_error_not_enough_space);
            } else {
                errorMsg = e.getMessage();
            }
            FileUtils.deleteQuietly(outputFile);
        } finally {
            stopForeground(true);
        }
        notify.showResult(errorMsg);
    }

    private void actionSmdFixChecksum(Intent intent) {
        String errorMsg = null;
        File romFile = new File(intent.getStringExtra("romPath"));

        if(!fileExists(romFile))
            return;

        SmdFixChecksum fixer = new SmdFixChecksum(this, romFile);

        Notify notify = new SmdFixChecksumNotify(this, romFile.getName());
        startForeground(notify.getID(), notify.getNotifyBuilder().build());

        try {
            fixer.fixChecksum();
        } catch (RomException | IOException e) {
            errorMsg = e.getMessage();
        } finally {
            stopForeground(true);
        }
        notify.showResult(errorMsg);
    }

    private void actionSnesAddSmcHeader(Intent intent) {
        String errorMsg = null;

        File romFile = new File(intent.getStringExtra("romPath"));
        String headerPath = intent.getStringExtra("headerPath");

        if(!fileExists(romFile))
            return;

        SnesSmcHeader worker = new SnesSmcHeader();

        Notify notify = new SnesAddSmcHeaderNotify(this, romFile.getName());
        startForeground(notify.getID(), notify.getNotifyBuilder().build());

        try {
            if (headerPath == null)
                worker.addSnesSmcHeader(this, romFile);
            else
                worker.addSnesSmcHeader(this, romFile, new File(headerPath));
        } catch (RomException | IOException e) {
            if (Utils.getFreeSpace(romFile.getParentFile()) == 0) {
                errorMsg = getString(R.string.notify_error_not_enough_space);
            } else {
                errorMsg = e.getMessage();
            }
        } finally {
            stopForeground(true);
        }
        notify.showResult(errorMsg);
    }

    private void actionSnesDeleteSmcHeader(Intent intent) {
        String errorMsg = null;

        File romFile = new File(intent.getStringExtra("romPath"));

        if(!fileExists(romFile))
            return;

        SnesSmcHeader worker = new SnesSmcHeader();

        Notify notify = new SnesDeleteSmcHeaderNotify(this, romFile.getName());
        startForeground(notify.getID(), notify.getNotifyBuilder().build());

        try {
            worker.deleteSnesSmcHeader(this, romFile, true);
        } catch (RomException | IOException e) {
            if (Utils.getFreeSpace(romFile.getParentFile()) == 0) {
                errorMsg = getString(R.string.notify_error_not_enough_space);
            } else {
                errorMsg = e.getMessage();
            }
        } finally {
            stopForeground(true);
        }
        notify.showResult(errorMsg);
    }

    private boolean fileExists(File f) {
        if (!f.exists() || f.isDirectory()) {
            String text = getString(R.string.notify_error_file_not_found).concat(": ").concat(f.getName());
            showErrorNotification(text);
            return false;
        }
        return true;
    }

    private void showErrorNotification(String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notify = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.notify_error))
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_stat_patching)
                .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(text))
                .build();
        nm.notify(0, notify);
    }
}