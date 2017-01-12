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

package org.emunix.unipatcher.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.emunix.unipatcher.R;
import org.emunix.unipatcher.Utils;
import org.markdown4j.Markdown4jProcessor;
import org.sufficientlysecure.htmltextview.HtmlResImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.IOException;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView versionText = (TextView) view.findViewById(R.id.versionText);
        versionText.setText(getString(R.string.help_activity_about_tab_version, Utils.getAppVersion(getActivity())));
        HtmlTextView aboutText = (HtmlTextView) view.findViewById(R.id.aboutText);
        try {
            String html = new Markdown4jProcessor().process(
                    getActivity().getResources().openRawResource(R.raw.about));
            aboutText.setHtml(html, new HtmlResImageGetter(aboutText));
        } catch (IOException e) {
            Log.e("UniPatcher", "IOException", e);
        }

        return view;
    }

}
