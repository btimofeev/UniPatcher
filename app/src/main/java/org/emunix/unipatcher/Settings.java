/*
Copyright (C) 2016, 2017 Boris Timofeev

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

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class Settings {

    public static String getLastRomDir(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("last_rom_directory", null);
    }

    public static void setLastRomDir(Context context, String directory) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_rom_directory", directory);
        editor.apply();
    }

    public static String getLastPatchDir(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("last_patch_directory", null);
    }

    public static void setLastPatchDir(Context context, String directory) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_patch_directory", directory);
        editor.apply();
    }

    public static String getRomDir(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("remember_last_directories", true)) {
            return getLastRomDir(context);
        } else
            return prefs.getString("rom_directory", "/");
    }

    public static String getPatchDir(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("remember_last_directories", true)) {
            return getLastPatchDir(context);
        } else
            return prefs.getString("patch_directory", "/");
    }

    public static String getOutputDir(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("output_directory", "");
    }

    public static boolean getIgnoreChecksum(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("ignore_checksum", false);
    }

    public static void setPatchingSuccessful(Context context, Boolean isSuccessful) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("patching_successful", isSuccessful);
        editor.apply();
    }

    public static boolean getPatchingSuccessful(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("patching_successful", false);
    }

    public static void setDontShowDonateSnackbarCount(Context context, int count) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("dont_show_donate_snackbar", count);
        editor.apply();
    }

    public static int getDontShowDonateSnackbarCount(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("dont_show_donate_snackbar", 0);
    }
}
