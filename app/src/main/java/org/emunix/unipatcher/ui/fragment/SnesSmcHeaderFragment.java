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

package org.emunix.unipatcher.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.emunix.unipatcher.Globals;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.Settings;
import org.emunix.unipatcher.Utils;
import org.emunix.unipatcher.WorkerService;
import org.emunix.unipatcher.tools.SnesSmcHeader;
import org.emunix.unipatcher.ui.activity.FilePickerActivity;

import java.io.File;

public class SnesSmcHeaderFragment extends ActionFragment implements View.OnClickListener {
    private static final String LOG_TAG = "org.emunix.unipatcher";
    private static final int SELECT_ROM_FILE = 1;
    private static final int SELECT_HEADER_FILE = 2;

    private TextView romNameTextView;
    private TextView headerNameTextView;
    private TextView headerInfoTextView;
    private CardView headerCardView;
    private String romPath = null;
    private String headerPath = null;

    private int action = 0;

    public SnesSmcHeaderFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.snes_smc_header_fragment, container, false);

        romNameTextView = (TextView) view.findViewById(R.id.romNameTextView);
        headerNameTextView = (TextView) view.findViewById(R.id.headerNameTextView);
        headerInfoTextView = (TextView) view.findViewById(R.id.headerInfoTextView);
        
        CardView romCardView = (CardView) view.findViewById(R.id.romCardView);
        romCardView.setOnClickListener(this);
        headerCardView = (CardView) view.findViewById(R.id.headerCardView);
        headerCardView.setOnClickListener(this);

        restoreState(savedInstanceState);

        setFonts(view);

        // Set action bar title
        getActivity().setTitle(R.string.nav_snes_add_del_smc_header);

        return view;
    }

    private void setFonts(View view) {
        TextView romLabel = (TextView) view.findViewById(R.id.romLabel);
        TextView headerLabel = (TextView) view.findViewById(R.id.headerLabel);

        Typeface roboto_light = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        romLabel.setTypeface(roboto_light);
        romNameTextView.setTypeface(roboto_light);
        headerLabel.setTypeface(roboto_light);
        headerNameTextView.setTypeface(roboto_light);
        headerInfoTextView.setTypeface(roboto_light);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            romPath = savedInstanceState.getString("romPath");
            headerPath = savedInstanceState.getString("headerPath");
            action = savedInstanceState.getInt("action");
            if (action == Globals.ACTION_SNES_ADD_SMC_HEADER) {
                headerInfoTextView.setText(R.string.snes_smc_header_will_be_added);
                headerCardView.setVisibility(View.VISIBLE);
            } else if (action == Globals.ACTION_SNES_DELETE_SMC_HEADER) {
                headerInfoTextView.setText(R.string.snes_smc_header_will_be_removed);
                headerCardView.setVisibility(View.GONE);
            }
            if (romPath != null)
                romNameTextView.setText(new File(romPath).getName());
            if (headerPath != null)
                headerNameTextView.setText(new File(headerPath).getName());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("romPath", romPath);
        savedInstanceState.putString("headerPath", headerPath);
        savedInstanceState.putInt("action", action);
    }

    @Override
    public void onClick(View view){
        Intent intent = new Intent(getActivity(), FilePickerActivity.class);
        switch (view.getId()) {
            case R.id.romCardView:
                intent.putExtra("title", getString(R.string.file_picker_activity_title_select_rom));
                intent.putExtra("directory", Settings.getRomDir(getActivity()));
                startActivityForResult(intent, SELECT_ROM_FILE);
                break;
            case R.id.headerCardView:
                intent.putExtra("title", getString(R.string.file_picker_activity_title_select_header));
                startActivityForResult(intent, SELECT_HEADER_FILE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(LOG_TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (resultCode == Activity.RESULT_OK) {
            String path = data.getStringExtra("path");

            if (Utils.isArchive(path)) {
                Toast.makeText(getActivity(), R.string.main_activity_toast_archives_not_supported, Toast.LENGTH_LONG).show();
            }

            switch (requestCode) {
                case SELECT_ROM_FILE:
                    romPath = path;
                    romNameTextView.setVisibility(View.VISIBLE);
                    romNameTextView.setText(new File(path).getName());
                    Settings.setLastRomDir(getActivity(), new File(path).getParent());
                    SnesSmcHeader checker = new SnesSmcHeader();
                    if (checker.isHasSmcHeader(new File(path))) {
                        action = Globals.ACTION_SNES_DELETE_SMC_HEADER;
                        headerCardView.setVisibility(View.GONE);
                        headerInfoTextView.setText(R.string.snes_smc_header_will_be_removed);
                    } else {
                        action = Globals.ACTION_SNES_ADD_SMC_HEADER;
                        headerCardView.setVisibility(View.VISIBLE);
                        headerInfoTextView.setText(R.string.snes_smc_header_will_be_added);
                    }
                    headerPath = null;
                    headerNameTextView.setText(R.string.main_activity_tap_to_select);
                    break;
                case SELECT_HEADER_FILE:
                    headerPath = path;
                    headerNameTextView.setText(new File(path).getName());
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean runAction(){
        if (romPath == null){
            Toast.makeText(getActivity(), getString(R.string.main_activity_toast_rom_not_selected), Toast.LENGTH_LONG).show();
            return false;
        }

        Intent intent = new Intent(getActivity(), WorkerService.class);
        intent.putExtra("action", action);
        intent.putExtra("romPath", romPath);
        intent.putExtra("headerPath", headerPath);
        getActivity().startService(intent);

        if (action == Globals.ACTION_SNES_ADD_SMC_HEADER) {
            Toast.makeText(getActivity(), R.string.notify_snes_add_smc_header_stared_check_noify, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.notify_snes_delete_smc_header_stared_check_noify, Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
