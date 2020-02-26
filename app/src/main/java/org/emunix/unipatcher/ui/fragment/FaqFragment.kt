/*
Copyright (C) 2016, 2020 Boris Timofeev

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
package org.emunix.unipatcher.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_faq.*
import org.emunix.unipatcher.R
import org.markdown4j.Markdown4jProcessor
import org.sufficientlysecure.htmltextview.HtmlResImageGetter
import java.io.IOException

class FaqFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_faq, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try {
            val html = Markdown4jProcessor().process(
                    activity!!.resources.openRawResource(R.raw.faq))
            faqText.setHtml(html, HtmlResImageGetter(activity!!))
        } catch (e: IOException) {
            Toast.makeText(activity, R.string.help_activity_error_cannot_load_text, Toast.LENGTH_LONG).show()
        }
    }
}