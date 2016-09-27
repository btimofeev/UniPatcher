/*
Copyright (C) 2016 Boris Timofeev

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

package org.emunix.unipatcher.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.emunix.unipatcher.ui.fragment.AboutFragment;
import org.emunix.unipatcher.ui.fragment.ChangelogFragment;
import org.emunix.unipatcher.ui.fragment.FaqFragment;

public class HelpPagerAdapter extends FragmentStatePagerAdapter {
    int numOfTabs;

    public HelpPagerAdapter(FragmentManager manager, int numOfTabs) {
        super(manager);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                FaqFragment tab1 = new FaqFragment();
                return tab1;
            case 1:
                ChangelogFragment tab2 = new ChangelogFragment();
                return tab2;
            case 2:
                AboutFragment tab3 = new AboutFragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
