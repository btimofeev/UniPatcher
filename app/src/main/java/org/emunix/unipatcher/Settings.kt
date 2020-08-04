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


object Settings {

    private val prefs = UniPatcher.appComponent.sharedPreferences()

    fun getLastRomDir(): String? {
        return prefs.getString("last_rom_directory", null)
    }

    fun setLastRomDir(directory: String) {
        val editor = prefs.edit()
        editor.putString("last_rom_directory", directory)
        editor.apply()
    }

    fun getLastPatchDir(): String? {
        return prefs.getString("last_patch_directory", null)
    }

    fun setLastPatchDir(directory: String) {
        val editor = prefs.edit()
        editor.putString("last_patch_directory", directory)
        editor.apply()
    }

    fun getRomDir(): String? {
        return if (prefs.getBoolean("remember_last_directories", true)) {
            getLastRomDir()
        } else
            prefs.getString("rom_directory", "/")
    }

    fun getPatchDir(): String? {
        return if (prefs.getBoolean("remember_last_directories", true)) {
            getLastPatchDir()
        } else
            prefs.getString("patch_directory", "/")
    }

    fun getOutputDir(): String {
        return prefs.getString("output_directory", "") ?: ""
    }

    fun getIgnoreChecksum(): Boolean {
        return prefs.getBoolean("ignore_checksum", false)
    }

    fun setPatchingSuccessful(isSuccessful: Boolean?) {
        val editor = prefs.edit()
        editor.putBoolean("patching_successful", isSuccessful!!)
        editor.apply()
    }

    fun getPatchingSuccessful(): Boolean {
        return prefs.getBoolean("patching_successful", false)
    }

    fun setDontShowDonateSnackbarCount(count: Int) {
        val editor = prefs.edit()
        editor.putInt("dont_show_donate_snackbar", count)
        editor.apply()
    }

    fun getDontShowDonateSnackbarCount(): Int {
        return prefs.getInt("dont_show_donate_snackbar", 0)
    }
}
