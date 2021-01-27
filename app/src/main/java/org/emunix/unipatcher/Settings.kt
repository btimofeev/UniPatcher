/*
Copyright (C) 2016, 2017, 2019, 2020 Boris Timofeev

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

package org.emunix.unipatcher

import androidx.core.content.edit


object Settings {

    private val prefs = UniPatcher.appComponent.sharedPreferences()

    fun getShowHelpButton(): Boolean {
        return prefs.getBoolean("show_how_to_use_app_button", true)
    }

    fun getIgnoreChecksum(): Boolean {
        return prefs.getBoolean("ignore_checksum", false)
    }

    fun setPatchingSuccessful(isSuccessful: Boolean?) {
        prefs.edit { putBoolean("patching_successful", isSuccessful!!) }
    }

    fun getPatchingSuccessful(): Boolean {
        return prefs.getBoolean("patching_successful", false)
    }

    fun setDontShowDonateSnackbarCount(count: Int) {
        prefs.edit { putInt("dont_show_donate_snackbar", count) }
    }

    fun getDontShowDonateSnackbarCount(): Int {
        return prefs.getInt("dont_show_donate_snackbar", 0)
    }
}
