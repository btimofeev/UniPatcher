/*
Copyright (C) 2013, 2016 Boris Timofeev

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

package org.emunix.unipatcher.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.emunix.unipatcher.Globals;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.Utils;
import org.emunix.unipatcher.ad.AdMobController;
import org.emunix.unipatcher.ui.adapter.FilePickerAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.zip.CRC32;


public class FilePickerActivity extends AppCompatActivity implements FilePickerAdapter.OnItemClickListener {

    private AdMobController ad;
    private RecyclerView list;
    private FilePickerAdapter listAdapter;
    private TextView permissionErrorText;

    private TextView crc32;
    private TextView md5;
    private TextView sha1;

    private List<FileEntry> fileList = new ArrayList<>();
    private File currentDir;
    private String savedCurrentDir;
    private String intentDir;

    private static final int REQUEST_PERMISSION_WRITE_STORAGE = 1;

    private static final String CRC32 = "CRC32";
    private static final String MD5 = "MD5";
    private static final String SHA1 = "SHA-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {/* empty */} // TODO log

        String title = getIntent().getStringExtra("title");
        if (title == null)
            title = getString(R.string.file_picker_activity_title);
        getSupportActionBar().setTitle(title);

        intentDir = getIntent().getStringExtra("directory");

        if (savedInstanceState != null) {
            savedCurrentDir = savedInstanceState.getString("currentDirectory");
        }

        permissionErrorText = (TextView) findViewById(R.id.empty_view);

        list = (RecyclerView) findViewById(R.id.list);
        try {
            list.setHasFixedSize(true);
        } catch (NullPointerException e) {/* TODO log */}
        RecyclerView.LayoutManager listLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(listLayoutManager);
        listAdapter = new FilePickerAdapter(fileList);
        list.setAdapter(listAdapter);
        listAdapter.setOnItemClickListener(this);

        requestStoragePermission();

        // Load ads
        if (!Globals.isFullVersion()) {
            FrameLayout adView = (FrameLayout) findViewById(R.id.adView);
            ad = new AdMobController(this, adView);
            if (!Utils.isOnline(this))
                ad.show(false);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position < 0 || position >= fileList.size()) // fix 'fast tapping' crash
            return;

        String fileName = fileList.get(position).getName();
        if (position == 0 && fileName.equals("..")) {
            browseTo(currentDir.getParentFile());
        } else {
            browseTo(new File(currentDir.getPath() + File.separator + fileName));
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        String fileName = currentDir.getPath() + File.separator + fileList.get(position).getName();
        showFileDetails(fileName);
    }

    private void readListOfFiles() {
        if (savedCurrentDir != null) {
            currentDir = new File(savedCurrentDir);
        } else if (intentDir == null) {
            currentDir = getExternalOrRoot();
        } else {
            currentDir = new File(intentDir);
        }

        if (!currentDir.canRead()) {
            String err = getString(R.string.file_picker_activity_error_unable_read_dir);
            err = String.format(err, currentDir.getAbsolutePath());
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();

            currentDir = getExternalOrRoot();
        }

        browseTo(currentDir);
    }

    private File getExternalOrRoot() {
        Boolean isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (isSDPresent) {
            return Environment.getExternalStorageDirectory().getAbsoluteFile();
        } else {
            return new File("/");
        }
    }

    private static File[] sortFiles(File[] files) {
        Comparator<File> comp = new Comparator<File>() {
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.compareTo(f2);
                }
            }
        };
        Arrays.sort(files, comp);
        return files;
    }

    private void browseTo(final File dir) {
        if (dir.isDirectory()) {
            currentDir = dir;
            fillFileList(sortFiles(dir.listFiles()));
        } else {
            Intent intent = new Intent();
            intent.putExtra("path", dir.getAbsolutePath());
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private void fillFileList(File[] files) {
        int size = fileList.size();
        fileList.clear();
        listAdapter.notifyItemRangeRemoved(0, size);

        FileEntry entry;

        if (currentDir.getParent() != null && currentDir.getParentFile().canRead()) {
            entry = new FileEntry();
            entry.setIcon(R.drawable.ic_folder_upload_grey600_24dp);
            entry.setName("..");
            fileList.add(entry);
        }

        for (File file : files) {
            if (file.isHidden() || !file.canRead())
                continue;
            if (file.isDirectory()) {
                entry = new FileEntry();
                entry.setIcon(R.drawable.ic_folder_grey600_24dp);
                entry.setName(file.getName());
                fileList.add(entry);
            } else {
                entry = new FileEntry();
                if (Utils.isPatch(file)){
                    entry.setIcon(R.drawable.ic_healing_grey600_24dp);
                } else {
                    entry.setIcon(R.drawable.ic_insert_drive_file_grey600_24dp);
                }
                entry.setName(file.getName());
                fileList.add(entry);
            }
        }

        listAdapter.notifyItemRangeInserted(0, fileList.size());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the Action Bar.
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (Utils.hasStoragePermission(this))
            savedInstanceState.putString("currentDirectory", currentDir.getAbsolutePath());
    }

    @Override
    public void onPause() {
        if (ad != null) {
            ad.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ad != null) {
            ad.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (ad != null) {
            ad.destroy();
        }
        super.onDestroy();
    }

    private void requestStoragePermission() {
        if (!Utils.hasStoragePermission(this)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE_STORAGE);
        } else {
            readListOfFiles();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showPermissionError(false);
                readListOfFiles();
            } else {
                showPermissionError(true);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showPermissionError(boolean on) {
        if (on) {
            list.setVisibility(View.GONE);
            if (ad != null)
                ad.show(false);
            permissionErrorText.setVisibility(View.VISIBLE);
        } else {
            permissionErrorText.setVisibility(View.GONE);
            if (ad != null)
                ad.show(true);
            list.setVisibility(View.VISIBLE);
        }
    }

    private void showFileDetails(String filename) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.file_properties_dialog_title)
                .customView(R.layout.fragment_file_details, true)
                .negativeText(R.string.file_properties_dialog_close_button)
                .build();

        TextView name = (TextView) dialog.getCustomView().findViewById(R.id.name_value);
        name.setText(FilenameUtils.getName(filename));

        TextView path = (TextView) dialog.getCustomView().findViewById(R.id.path_value);
        path.setText(FilenameUtils.getPath(filename));

        File file = new File(filename);
        long filesize = file.length();

        TextView size = (TextView) dialog.getCustomView().findViewById(R.id.size_value);
        String svFmt = getString(R.string.file_properties_dialog_size_value);
        size.setText(String.format(svFmt, FileUtils.byteCountToDisplaySize(filesize), filesize));

        crc32 = (TextView) dialog.getCustomView().findViewById(R.id.crc32_value);
        md5 = (TextView) dialog.getCustomView().findViewById(R.id.md5_value);
        sha1 = (TextView) dialog.getCustomView().findViewById(R.id.sha1_value);
        new FileChecksumsTask().execute(file);

        if (this.hasWindowFocus())
            dialog.show();
    }

    static public class FileEntry {
        private int icon;
        private String name;

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private class FileChecksumsTask extends AsyncTask<File, Void, HashMap<String, String>> {
        @Override
        protected HashMap<String, String> doInBackground(File... params) {
            HashMap<String, String> checksum = null;
            try {
                if (params.length > 0)
                    checksum = getFileChecksums(params[0]);
            } catch (NoSuchAlgorithmException | IOException | IllegalArgumentException e) {
                e.printStackTrace();
            }
            return checksum;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> result) {
            super.onPostExecute(result);
            if (result != null) {
                crc32.setText(result.get(CRC32));
                md5.setText(result.get(MD5));
                sha1.setText(result.get(SHA1));
            } else {
                crc32.setText("-");
                md5.setText("-");
                sha1.setText("-");
            }
        }

        private HashMap<String, String> getFileChecksums(File file) throws IOException, NoSuchAlgorithmException, IllegalArgumentException
        {
            if (file.isDirectory())
                throw new IllegalArgumentException("Unable calculate checksum for directory");

            FileInputStream fis = new FileInputStream(file);

            CRC32 crc32Digest = new CRC32();
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");

            byte[] byteArray = new byte[32768];
            int bytesCount = 0;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                crc32Digest.update(byteArray, 0, bytesCount);
                md5Digest.update(byteArray, 0, bytesCount);
                sha1Digest.update(byteArray, 0, bytesCount);
            }
            fis.close();

            String crc32 = Long.toHexString(crc32Digest.getValue());
            String md5 = bytesToHexString(md5Digest.digest());
            String sha1 = bytesToHexString(sha1Digest.digest());

            HashMap<String, String> checksum = new HashMap<>();
            checksum.put(CRC32, crc32);
            checksum.put(MD5, md5);
            checksum.put(SHA1, sha1);
            return checksum;
        }

        private String bytesToHexString(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < bytes.length ;i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        }
    }
}
