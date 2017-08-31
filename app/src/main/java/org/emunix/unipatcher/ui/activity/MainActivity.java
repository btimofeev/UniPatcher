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

package org.emunix.unipatcher.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import org.emunix.unipatcher.BuildConfig;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.UniPatcher;
import org.emunix.unipatcher.ui.fragment.ActionFragment;
import org.emunix.unipatcher.ui.fragment.CreatePatchFragment;
import org.emunix.unipatcher.ui.fragment.PatchingFragment;
import org.emunix.unipatcher.ui.fragment.SmdFixChecksumFragment;
import org.emunix.unipatcher.ui.fragment.SnesSmcHeaderFragment;

import static android.support.v7.app.AppCompatDelegate.MODE_NIGHT_AUTO;
import static android.support.v7.app.AppCompatDelegate.MODE_NIGHT_NO;
import static android.support.v7.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                ActionFragment fragment = (ActionFragment) fragmentManager.findFragmentById(R.id.content_frame);
                if (fragment != null) {
                    boolean ret = fragment.runAction();
                }
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            selectDrawerItem(0);
        }

        parseArgument();
    }

    private void setTheme() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString("theme", "light");
        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                break;
            case "daynight":
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO);
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_apply_patch) {
            selectDrawerItem(0);
        } else if (id == R.id.nav_create_patch) {
            selectDrawerItem(1);
        } else if (id == R.id.nav_smd_fix_checksum) {
            selectDrawerItem(2);
        } else if (id == R.id.nav_snes_add_del_smc_header) {
            selectDrawerItem(3);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (id == R.id.nav_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        } else if (id == R.id.nav_rate) {
            rateApp();
        } else if (id == R.id.nav_donate) {
            Intent donateIntent = new Intent(this, DonateActivity.class);
            startActivity(donateIntent);
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_help) {
            Intent helpIntent = new Intent(this, HelpActivity.class);
            startActivity(helpIntent);
        }

        return true;
    }

    private void selectDrawerItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment;
        switch (position) {
            case 1:
                fragment = new CreatePatchFragment();
                break;
            case 2:
                fragment = new SmdFixChecksumFragment();
                break;
            case 3:
                fragment = new SnesSmcHeaderFragment();
                break;
            default:
                fragment = new PatchingFragment();
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out);
        ft.replace(R.id.content_frame, fragment).commit();
    }

    private void parseArgument() {
        try {
            String arg = getIntent().getData().getPath();
            UniPatcher.setAppArgument(arg);
        } catch (NullPointerException e) {
            // The application is not opened from the file manager
        }
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + BuildConfig.SHARE_URL);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_dialog_title)));
    }

    public void rateApp() {
        Intent rateAppIntent = new Intent(Intent.ACTION_VIEW);
        rateAppIntent.setData(Uri.parse(BuildConfig.RATE_URL));
        if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() == 0) {
            // Market app is not installed. Open web browser
            rateAppIntent.setData(Uri.parse(BuildConfig.SHARE_URL));
        }
        startActivity(rateAppIntent);
    }
}
