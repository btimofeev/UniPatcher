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

package org.emunix.unipatcher.ad;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.emunix.unipatcher.Globals;

public class AdMobController implements AdsController {

    private static final String ADMOB_ID = "ca-app-pub-2445378722408015/8831379284";
    private AdView adView;
    private final Context context;

    public AdMobController(Context c, FrameLayout layout) {
        context = c;
        createView(layout);
    }

    public void createView(FrameLayout layout) {
        adView = new AdView(context);
        adView.setAdSize(com.google.android.gms.ads.AdSize.SMART_BANNER);
        adView.setAdUnitId(ADMOB_ID);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("018B7216B142ABE97F9BFCD880C02EBC")  // htc one v
                .addTestDevice("64F8D0EE579BA448E833172DB2D91CBB")  // lg nexus 5
                .build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (Globals.isFullVersion()) {
                    adView.setVisibility(View.GONE);
                }
            }
            @Override
            public void onAdOpened() {
            }
        });
        adView.loadAd(adRequest);
        layout.addView(adView);
    }

    public void show(boolean show) {
        if (adView != null) {
            if (!show) {
                adView.setVisibility(View.GONE);
            } else if (!Globals.isFullVersion()) {
                adView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void pause() {
        if (adView != null) {
            adView.pause();
        }
    }

    public void resume() {
        if (adView != null) {
            adView.resume();
        }
    }

    public void start() {}

    public void destroy() {
        if (adView != null) {
            adView.destroy();
        }
    }
}
