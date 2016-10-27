/*
Copyright (C) 2013-2016 Boris Timofeev

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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.emunix.unipatcher.BuildConfig;
import org.emunix.unipatcher.Globals;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.Utils;
import org.emunix.unipatcher.ad.AdMobController;
import org.emunix.unipatcher.ui.dialog.RateThisApp;
import org.emunix.unipatcher.ui.fragment.ActionFragment;
import org.emunix.unipatcher.ui.fragment.PatchingFragment;
import org.emunix.unipatcher.ui.fragment.SmdFixChecksumFragment;
import org.emunix.unipatcher.ui.fragment.SnesSmcHeaderFragment;

import static android.support.v7.app.AppCompatDelegate.MODE_NIGHT_AUTO;
import static android.support.v7.app.AppCompatDelegate.MODE_NIGHT_NO;
import static android.support.v7.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends AppCompatActivity
     implements NavigationView.OnNavigationItemSelectedListener {
    private static final String LOG_TAG = "org.emunix.unipatcher";

    private static final String SKU_FULL = "full";
    private static final String SKU_REMOVE_ADS = "ad";
    private boolean readyToPurchase = false;
    private BillingProcessor bp;
    private AdMobController ad;
    private FirebaseAnalytics firebaseAnalytics;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (BuildConfig.DEBUG)
            firebaseAnalytics.setAnalyticsCollectionEnabled(false);

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
                if (fragment != null){
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
        bp = new BillingProcessor(this, Globals.getKey(), new BillingProcessor.IBillingHandler() {
            @Override
            public void onBillingInitialized() {
                Log.d(LOG_TAG, "Billing initialized");
                readyToPurchase = true;
                if (bp.isPurchased(SKU_FULL) || bp.isPurchased(SKU_REMOVE_ADS)) {
                    setFullVersion();
                }
            }
            @Override
            public void onProductPurchased(String productId, TransactionDetails details) {
                Log.d(LOG_TAG, "Item purchased: " + productId);
                complain(getString(R.string.purchase_successful));
                setFullVersion();
            }
            @Override
            public void onBillingError(int errorCode, Throwable error) {
                if (errorCode != 110) // cancel purchase
                    complain("Billing error: " + Integer.toString(errorCode));
            }
            @Override
            public void onPurchaseHistoryRestored() {
                for(String sku : bp.listOwnedProducts())
                    Log.d(LOG_TAG, "Owned Managed Product: " + sku);
                if (bp.isPurchased(SKU_FULL) || bp.isPurchased(SKU_REMOVE_ADS)) {
                    setFullVersion();
                }
            }
        });
        RateThisApp.launch(this);

        // Load ads
        if (!Globals.isFullVersion()) {
            Handler adHandler = new Handler();
            adHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FrameLayout adView = (FrameLayout) findViewById(R.id.adView);
                    ad = new AdMobController(context, adView);
                    if (!Utils.isOnline(context))
                        ad.show(false);
                }
            }, 1000);
        }
    }

    private void setTheme() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString("theme","light");
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
        } else if (id == R.id.nav_smd_fix_checksum) {
            selectDrawerItem(1);
        } else if (id == R.id.nav_snes_add_del_smc_header) {
            selectDrawerItem(2);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (id == R.id.nav_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        } else if (id == R.id.nav_rate) {
            RateThisApp.rate(this);
        } else if (id == R.id.nav_buy) {
            buyFullVersion();
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
            case 1: fragment = new SmdFixChecksumFragment(); break;
            case 2: fragment = new SnesSmcHeaderFragment(); break;
            default: fragment = new PatchingFragment();
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out);
        ft.replace(R.id.content_frame, fragment).commit();
    }

    private void parseArgument() {
        try {
            String arg = getIntent().getData().getPath();
            Globals.setCmdArgument(arg);
            Log.d(LOG_TAG, "Cmd argument: " + arg);
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "NullPointerException in argument fetching");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(LOG_TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + "https://play.google.com/store/apps/details?id=org.eminix.unipatcher");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_dialog_title)));
    }

    private void buyFullVersion() {
        if (readyToPurchase)
            bp.purchase(this, SKU_REMOVE_ADS);
        else
            complain("Billing not initialized.");
    }

    private void setFullVersion() {
        Globals.setFullVersion();
        if (ad != null)
            ad.show(false);
    }

    private void complain(String message) {
        Log.d(LOG_TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
        if (bp != null) {
            bp.release();
        }
        if (ad != null) {
            ad.destroy();
        }
        super.onDestroy();
    }
}
