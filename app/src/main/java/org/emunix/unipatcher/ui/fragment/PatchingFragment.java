/*
Copyright (C) 2014, 2016, 2017 Boris Timofeev

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

package org.emunix.unipatcher.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;
import org.emunix.unipatcher.Globals;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.Settings;
import org.emunix.unipatcher.Utils;
import org.emunix.unipatcher.WorkerService;
import org.emunix.unipatcher.ui.activity.FilePickerActivity;

import java.io.File;

public class PatchingFragment extends ActionFragment implements View.OnClickListener {

    private static final String LOG_TAG = "org.emunix.unipatcher";

    private static final int SELECT_ROM_FILE = 1;
    private static final int SELECT_PATCH_FILE = 2;
    private TextView romNameTextView;
    private TextView patchNameTextView;
    private TextView outputNameTextView;
    private String romPath = null;
    private String patchPath = null;
    private String outputPath = null;

    public PatchingFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patching_fragment, container, false);

        romNameTextView = (TextView) view.findViewById(R.id.romNameTextView);
        patchNameTextView = (TextView) view.findViewById(R.id.patchNameTextView);
        outputNameTextView = (TextView) view.findViewById(R.id.outputNameTextView);

        CardView patchCardView = (CardView) view.findViewById(R.id.patchCardView);
        patchCardView.setOnClickListener(this);
        CardView romCardView = (CardView) view.findViewById(R.id.romCardView);
        romCardView.setOnClickListener(this);
        CardView outputCardView = (CardView) view.findViewById(R.id.outputCardView);
        outputCardView.setOnClickListener(this);

        restoreState(savedInstanceState);

        setFonts(view);

        // Set action bar title
        getActivity().setTitle(R.string.nav_apply_patch);

        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parseArgument();
    }

    private void parseArgument() {
        patchPath = Globals.getCmdArgument();
        if (patchPath != null) {
            patchNameTextView.setText(new File(patchPath).getName());
        }
    }

    private void setFonts(View view) {
        TextView patchLabel = (TextView) view.findViewById(R.id.patchLabel);
        TextView romLabel = (TextView) view.findViewById(R.id.romLabel);
        TextView outputLabel = (TextView) view.findViewById(R.id.outputLabel);

        Typeface roboto_light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        patchLabel.setTypeface(roboto_light);
        romLabel.setTypeface(roboto_light);
        outputLabel.setTypeface(roboto_light);
        patchNameTextView.setTypeface(roboto_light);
        romNameTextView.setTypeface(roboto_light);
        outputNameTextView.setTypeface(roboto_light);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            romPath = savedInstanceState.getString("romPath");
            patchPath = savedInstanceState.getString("patchPath");
            outputPath = savedInstanceState.getString("outputPath");
            if (romPath != null)
                romNameTextView.setText(new File(romPath).getName());
            if (patchPath != null)
                patchNameTextView.setText(new File (patchPath).getName());
            if (outputPath != null)
                outputNameTextView.setText(new File(outputPath).getName());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("romPath", romPath);
        savedInstanceState.putString("patchPath", patchPath);
        savedInstanceState.putString("outputPath", outputPath);
    }

    @Override
    public void onClick(View view){
        Intent intent = new Intent(getActivity(), FilePickerActivity.class);
        switch (view.getId()) {
            case R.id.patchCardView:
                intent.putExtra("title", getString(R.string.file_picker_activity_title_select_patch));
                intent.putExtra("directory", Settings.getPatchDir(getActivity()));
                startActivityForResult(intent, SELECT_PATCH_FILE);
                break;
            case R.id.romCardView:
                intent.putExtra("title", getString(R.string.file_picker_activity_title_select_rom));
                intent.putExtra("directory", Settings.getRomDir(getActivity()));
                startActivityForResult(intent, SELECT_ROM_FILE);
                break;
            case R.id.outputCardView:
                renameOutputRom();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(LOG_TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (resultCode == Activity.RESULT_OK) {
            String path = data.getStringExtra("path");
            File fpath = new File(path);

            if (Utils.isArchive(path)) {
                Toast.makeText(getActivity(), R.string.main_activity_toast_archives_not_supported, Toast.LENGTH_LONG).show();
            }

            switch (requestCode) {
                    case SELECT_ROM_FILE:
                        romPath = path;
                        romNameTextView.setVisibility(View.VISIBLE);
                        romNameTextView.setText(fpath.getName());
                        Settings.setLastRomDir(getActivity(), fpath.getParent());
                        outputPath = makeOutputPath(path);
                        outputNameTextView.setText(new File(outputPath).getName());
                        break;
                    case SELECT_PATCH_FILE:
                        patchPath = path;
                        patchNameTextView.setVisibility(View.VISIBLE);
                        patchNameTextView.setText(fpath.getName());
                        Settings.setLastPatchDir(getActivity(), fpath.getParent());
                        break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String makeOutputPath(String fullname) {
        String dir = Settings.getOutputDir(getActivity());
        if (dir.equals("")) { // get ROM directory
            dir = FilenameUtils.getFullPath(fullname);
        }
        String baseName = FilenameUtils.getBaseName(fullname);
        String ext = FilenameUtils.getExtension(fullname);
        return FilenameUtils.concat(dir, baseName.concat(" [patched].").concat(ext));
    }

    public boolean runAction(){
        if (romPath == null & patchPath == null){
            Toast.makeText(getActivity(), getString(R.string.main_activity_toast_rom_and_patch_not_selected), Toast.LENGTH_LONG).show();
            return false;
        } else if (romPath == null){
            Toast.makeText(getActivity(), getString(R.string.main_activity_toast_rom_not_selected), Toast.LENGTH_LONG).show();
            return false;
        } else if (patchPath == null){
            Toast.makeText(getActivity(), getString(R.string.main_activity_toast_patch_not_selected), Toast.LENGTH_LONG).show();
            return false;
        }

        Intent intent = new Intent(getActivity(), WorkerService.class);
        intent.putExtra("action", Globals.ACTION_PATCHING);
        intent.putExtra("romPath", romPath);
        intent.putExtra("patchPath", patchPath);
        intent.putExtra("outputPath", outputPath);
        getActivity().startService(intent);

        Toast.makeText(getActivity(), R.string.toast_patching_started_check_notify,Toast.LENGTH_SHORT).show();
        return true;
    }

    private void renameOutputRom(){
        if (romPath == null) {
            Toast.makeText(getActivity(), getString(R.string.main_activity_toast_rom_not_selected), Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.dialog_rename_title);
        final EditText input = new EditText(getActivity());
        input.setText(outputNameTextView.getText());

        // add left and right margins to EditText.
        FrameLayout container = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int dp_24 = Utils.dpToPx(getActivity(), 24);
        params.setMargins(dp_24, 0, dp_24, 0);
        input.setLayoutParams(params);
        container.addView(input);
        dialog.setView(container);

        dialog.setPositiveButton(R.string.dialog_rename_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                if (newName.contains("/")) {
                    newName = newName.replaceAll("/", "_");
                    Toast.makeText(getActivity(), R.string.dialog_rename_error_invalid_chars, Toast.LENGTH_LONG).show();
                }
                String newPath = new File(outputPath).getParent().concat(File.separator).concat(newName);
                if (FilenameUtils.equals(newPath, romPath)) {
                    Toast.makeText(getActivity(), R.string.dialog_rename_error_same_name, Toast.LENGTH_LONG).show();
                    return;
                }
                outputNameTextView.setText(newName);
                outputPath = newPath;
            }
        });
        dialog.setNegativeButton(R.string.dialog_rename_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog.show();
    }
}
