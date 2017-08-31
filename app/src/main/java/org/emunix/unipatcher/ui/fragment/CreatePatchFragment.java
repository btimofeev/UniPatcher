/*
 Copyright (c) 2017 Boris Timofeev

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
import org.emunix.unipatcher.Action;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.Settings;
import org.emunix.unipatcher.Utils;
import org.emunix.unipatcher.WorkerService;
import org.emunix.unipatcher.ui.activity.FilePickerActivity;

import java.io.File;

public class CreatePatchFragment extends ActionFragment implements View.OnClickListener {

    private static final String LOG_TAG = "org.emunix.unipatcher";

    private TextView sourceNameTextView;
    private TextView modifiedNameTextView;
    private TextView patchNameTextView;
    private String sourcePath = null;
    private String modifiedPath = null;
    private String patchPath = null;

    public CreatePatchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_patch_fragment, container, false);

        sourceNameTextView = (TextView) view.findViewById(R.id.sourceFileNameTextView);
        modifiedNameTextView = (TextView) view.findViewById(R.id.modifiedFileNameTextView);
        patchNameTextView = (TextView) view.findViewById(R.id.patchFileNameTextView);

        CardView sourceCardView = (CardView) view.findViewById(R.id.sourceFileCardView);
        sourceCardView.setOnClickListener(this);
        CardView modifiedCardView = (CardView) view.findViewById(R.id.modifiedFileCardView);
        modifiedCardView.setOnClickListener(this);
        CardView patchCardView = (CardView) view.findViewById(R.id.patchFileCardView);
        patchCardView.setOnClickListener(this);

        restoreState(savedInstanceState);

        setFonts(view);

        // Set action bar title
        getActivity().setTitle(R.string.nav_create_patch);

        return view;
    }

    private void setFonts(View view) {
        TextView sourceLabel = (TextView) view.findViewById(R.id.sourceFileLabel);
        TextView modifiedLabel = (TextView) view.findViewById(R.id.modifiedFileLabel);
        TextView patchLabel = (TextView) view.findViewById(R.id.patchFileLabel);

        Typeface roboto_light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        sourceLabel.setTypeface(roboto_light);
        modifiedLabel.setTypeface(roboto_light);
        patchLabel.setTypeface(roboto_light);
        sourceNameTextView.setTypeface(roboto_light);
        modifiedNameTextView.setTypeface(roboto_light);
        patchNameTextView.setTypeface(roboto_light);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            sourcePath = savedInstanceState.getString("sourcePath");
            modifiedPath = savedInstanceState.getString("modifiedPath");
            patchPath = savedInstanceState.getString("patchPath");
            if (sourcePath != null)
                sourceNameTextView.setText(new File(sourcePath).getName());
            if (modifiedPath != null)
                modifiedNameTextView.setText(new File(modifiedPath).getName());
            if (patchPath != null)
                patchNameTextView.setText(new File(patchPath).getName());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("sourcePath", sourcePath);
        savedInstanceState.putString("modifiedPath", modifiedPath);
        savedInstanceState.putString("patchPath", patchPath);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getActivity(), FilePickerActivity.class);
        switch (view.getId()) {
            case R.id.sourceFileCardView:
                intent.putExtra("title", getString(R.string.file_picker_activity_title_select_source_file));
                intent.putExtra("directory", Settings.getRomDir(getActivity()));
                startActivityForResult(intent, Action.SELECT_SOURCE_FILE);
                break;
            case R.id.modifiedFileCardView:
                intent.putExtra("title", getString(R.string.file_picker_activity_title_select_modified_file));
                intent.putExtra("directory", Settings.getRomDir(getActivity()));
                startActivityForResult(intent, Action.SELECT_MODIFIED_FILE);
                break;
            case R.id.patchFileCardView:
                renamePatchFile();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (resultCode == Activity.RESULT_OK) {
            String path = data.getStringExtra("path");
            File fpath = new File(path);

            switch (requestCode) {
                case Action.SELECT_SOURCE_FILE:
                    sourcePath = path;
                    sourceNameTextView.setVisibility(View.VISIBLE);
                    sourceNameTextView.setText(fpath.getName());
                    Settings.setLastRomDir(getActivity(), fpath.getParent());
                    break;
                case Action.SELECT_MODIFIED_FILE:
                    modifiedPath = path;
                    modifiedNameTextView.setVisibility(View.VISIBLE);
                    modifiedNameTextView.setText(fpath.getName());
                    Settings.setLastRomDir(getActivity(), fpath.getParent());
                    patchPath = makeOutputPath(path);
                    patchNameTextView.setText(new File(patchPath).getName());
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
        return FilenameUtils.concat(dir, baseName.concat(".xdelta"));
    }

    public boolean runAction() {
        if (sourcePath == null & modifiedPath == null) {
            Toast.makeText(getActivity(), getString(R.string.create_patch_fragment_toast_source_and_modified_not_selected), Toast.LENGTH_LONG).show();
            return false;
        } else if (sourcePath == null) {
            Toast.makeText(getActivity(), getString(R.string.create_patch_fragment_toast_source_not_selected), Toast.LENGTH_LONG).show();
            return false;
        } else if (modifiedPath == null) {
            Toast.makeText(getActivity(), getString(R.string.create_patch_fragment_toast_modified_not_selected), Toast.LENGTH_LONG).show();
            return false;
        }

        Intent intent = new Intent(getActivity(), WorkerService.class);
        intent.putExtra("action", Action.CREATE_PATCH);
        intent.putExtra("sourcePath", sourcePath);
        intent.putExtra("modifiedPath", modifiedPath);
        intent.putExtra("patchPath", patchPath);
        getActivity().startService(intent);

        Toast.makeText(getActivity(), R.string.toast_create_patch_started_check_notify, Toast.LENGTH_SHORT).show();
        return true;
    }

    private void renamePatchFile() {
        if (modifiedPath == null) {
            Toast.makeText(getActivity(), getString(R.string.create_patch_fragment_toast_modified_not_selected), Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.dialog_rename_title);
        final EditText input = new EditText(getActivity());
        input.setText(patchNameTextView.getText());

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
                if (newName.equals("")) {
                    Toast.makeText(getActivity(), R.string.dialog_rename_error_empty_name, Toast.LENGTH_LONG).show();
                    return;
                }
                if (newName.contains("/")) {
                    newName = newName.replaceAll("/", "_");
                    Toast.makeText(getActivity(), R.string.dialog_rename_error_invalid_chars, Toast.LENGTH_LONG).show();
                }
                String newPath = new File(patchPath).getParent().concat(File.separator).concat(newName);
                if (FilenameUtils.equals(newPath, sourcePath) || FilenameUtils.equals(newPath, modifiedPath)) {
                    Toast.makeText(getActivity(), R.string.dialog_rename_error_same_name, Toast.LENGTH_LONG).show();
                    return;
                }
                patchNameTextView.setText(newName);
                patchPath = newPath;
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