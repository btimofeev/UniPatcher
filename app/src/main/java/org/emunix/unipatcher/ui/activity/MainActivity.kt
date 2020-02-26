/*
Copyright (C) 2013-2017, 2019-2020 Boris Timofeev

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
package org.emunix.unipatcher.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import org.emunix.unipatcher.BuildConfig
import org.emunix.unipatcher.R
import org.emunix.unipatcher.Settings.getDontShowDonateSnackbarCount
import org.emunix.unipatcher.Settings.getPatchingSuccessful
import org.emunix.unipatcher.Settings.setDontShowDonateSnackbarCount
import org.emunix.unipatcher.ui.fragment.*
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    @JvmField
    var arg: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            val fragmentManager = supportFragmentManager
            val fragment = fragmentManager.findFragmentById(R.id.content_frame) as ActionFragment?
            if (fragment != null) {
                val ret = fragment.runAction()
            }
        }
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        if (savedInstanceState == null) {
            selectDrawerItem(0)
        }
        parseArgument()
        showDonateSnackbar()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_apply_patch -> selectDrawerItem(0)
            R.id.nav_create_patch -> selectDrawerItem(1)
            R.id.nav_smd_fix_checksum -> selectDrawerItem(2)
            R.id.nav_snes_add_del_smc_header -> selectDrawerItem(3)
            R.id.nav_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent)
            }
            R.id.nav_rate -> rateApp()
            R.id.nav_donate -> showDonateActivity()
            R.id.nav_share -> shareApp()
            R.id.nav_help -> {
                val helpIntent = Intent(this, HelpActivity::class.java)
                startActivity(helpIntent)
            }
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun selectDrawerItem(position: Int) { // update the main content by replacing fragments
        val fragment: Fragment = when (position) {
            1 -> CreatePatchFragment()
            2 -> SmdFixChecksumFragment()
            3 -> SnesSmcHeaderFragment()
            else -> PatchingFragment()
        }
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out)
        ft.replace(R.id.content_frame, fragment).commit()
    }

    private fun parseArgument() {
        arg = intent?.data?.path
        Timber.d("Path to file from intent that started this activity: %s", arg)
    }

    private fun showDonateSnackbar() { // don't show snackbar if the user did not patch the file successfully
        if (!getPatchingSuccessful(this)) return
        // don't show snackbar some time if the user swiped off it before
        var count = getDontShowDonateSnackbarCount(this)
        if (count != 0) {
            setDontShowDonateSnackbarCount(this, --count)
            return
        }
        // don't show snackbar each time you open the application
        if (Random().nextInt(6) != 0) return
        Snackbar.make(findViewById(R.id.content_frame), R.string.main_activity_donate_snackbar_text, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.main_activity_donate_snackbar_button) { showDonateActivity() }
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(snackbar: Snackbar, event: Int) {
                        if (event == DISMISS_EVENT_SWIPE) {
                            setDontShowDonateSnackbarCount(applicationContext, 30)
                        }
                    }
                }
                ).show()
    }

    private fun showDonateActivity() {
        val donateIntent = Intent(this, DonateActivity::class.java)
        startActivity(donateIntent)
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + BuildConfig.SHARE_URL)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_dialog_title)))
    }

    private fun rateApp() {
        val rateAppIntent = Intent(Intent.ACTION_VIEW)
        rateAppIntent.data = Uri.parse(BuildConfig.RATE_URL)
        if (packageManager.queryIntentActivities(rateAppIntent, 0).size == 0) { // Market app is not installed. Open web browser
            rateAppIntent.data = Uri.parse(BuildConfig.SHARE_URL)
        }
        startActivity(rateAppIntent)
    }
}