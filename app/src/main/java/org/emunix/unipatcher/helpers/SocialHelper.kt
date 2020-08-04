/*
 Copyright (c) 2013-2020 Boris Timofeev

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

package org.emunix.unipatcher.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.emunix.unipatcher.BuildConfig
import org.emunix.unipatcher.R
import javax.inject.Inject

class SocialHelper @Inject constructor(val context: Context) {

    fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
        shareIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text) + BuildConfig.SHARE_URL)
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val chooseIntent = Intent.createChooser(shareIntent, context.getString(R.string.share_dialog_title))
        chooseIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(chooseIntent)
    }

    fun rateApp() {
        val rateAppIntent = Intent(Intent.ACTION_VIEW)
        rateAppIntent.data = Uri.parse(BuildConfig.RATE_URL)
        if (context.packageManager.queryIntentActivities(rateAppIntent, 0).size == 0) { // Market app is not installed. Open web browser
            rateAppIntent.data = Uri.parse(BuildConfig.SHARE_URL)
        }
        rateAppIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(rateAppIntent)
    }

}