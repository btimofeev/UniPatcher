/*
Copyright (C) 2013 Boris Timofeev

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

package org.emunix.unipatcher.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import org.emunix.unipatcher.R;

public class RateThisApp {

    private final static int LAUNCHES_UNTIL_PROMPT = 7;

    public static void launch(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("RateThisApp", 0);
        if (preferences.getBoolean("dont_show_again", false))
            return;

        SharedPreferences.Editor editor = preferences.edit();
        long launchCount = preferences.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launchCount);

        if (launchCount % LAUNCHES_UNTIL_PROMPT == 0)
            showDialog(context);

        editor.apply();
    }

    public static void rate(Context context) {
        String packageName = context.getApplicationContext().getPackageName();
        Intent rateAppIntent = new Intent(Intent.ACTION_VIEW);
        rateAppIntent.setData(Uri.parse("market://details?id=" + packageName));
        if (context.getPackageManager().queryIntentActivities(rateAppIntent, 0).size() == 0) {
            // Market app is not installed. Open web browser
            rateAppIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
        }
        context.startActivity(rateAppIntent);
        dontShowDialogAgain(context);
    }

    private static void showDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.rate_dialog_title);
        builder.setMessage(R.string.rate_dialog_message);
        builder.setPositiveButton(R.string.rate_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                rate(context);
            }
        });

        builder.setNegativeButton(R.string.rate_dialog_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static void dontShowDialogAgain(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("RateThisApp", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("dont_show_again", true);
        editor.apply();
    }
}
