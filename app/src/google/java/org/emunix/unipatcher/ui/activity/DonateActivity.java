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

package org.emunix.unipatcher.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import org.emunix.unipatcher.BuildConfig;
import org.emunix.unipatcher.R;
import org.sufficientlysecure.donations.DonationsFragment;

public class DonateActivity extends AppCompatActivity {

    private static final String[] GOOGLE_PLAY_CATALOG = new String[]{"donate_1", "donate_3",
            "donate_5", "donate_10", "donate_25", "donate_50", "donate_100"};
    private static final String[] GOOGLE_PLAY_COST = new String[]{"$1", "$3", "$5", "$10",
            "$25", "$50", "$100"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            /* empty */
        }
        getSupportActionBar().setTitle(R.string.donate_activity_title);

        DonationsFragment fragment = (DonationsFragment) getSupportFragmentManager().findFragmentByTag("donationsFragment");
        if (fragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            fragment = DonationsFragment.newInstance(BuildConfig.DEBUG,
                    true, BuildConfig.GOOGLE_PLAY_PUBKEY, GOOGLE_PLAY_CATALOG, GOOGLE_PLAY_COST,
                    false, null, null, null,
                    false, null, null,
                    false, null);

            ft.replace(R.id.donate_fragment, fragment, "donationsFragment");
            ft.commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("donationsFragment");
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
