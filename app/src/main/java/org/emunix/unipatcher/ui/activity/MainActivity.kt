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
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import org.emunix.unipatcher.BuildConfig
import org.emunix.unipatcher.R
import org.emunix.unipatcher.Settings.getDontShowDonateSnackbarCount
import org.emunix.unipatcher.Settings.getPatchingSuccessful
import org.emunix.unipatcher.Settings.setDontShowDonateSnackbarCount
import org.emunix.unipatcher.databinding.ActivityMainBinding
import org.emunix.unipatcher.ui.fragment.*
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    enum class NavigateTo {
        APPLY_PATCH, CREATE_PATCH, SMD_FIX_CHECKSUM, SNES_SMC_HEADER
    }

    @JvmField
    var arg: String? = null

    private lateinit var _binding: ActivityMainBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.includes.toolbar)
        val toggle = ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.includes.toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.fab.setOnClickListener {
            val fragmentManager = supportFragmentManager
            val fragment = fragmentManager.findFragmentById(R.id.content_frame) as ActionFragment?
            if (fragment != null) {
                val ret = fragment.runAction()
            }
        }
        binding.navigationView.setNavigationItemSelectedListener(this)
        if (savedInstanceState == null) {
            replaceFragment(NavigateTo.APPLY_PATCH)
        }
        parseArgument()
        showDonateSnackbar()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_apply_patch -> replaceFragment(NavigateTo.APPLY_PATCH)
            R.id.nav_create_patch -> replaceFragment(NavigateTo.CREATE_PATCH)
            R.id.nav_smd_fix_checksum -> replaceFragment(NavigateTo.SMD_FIX_CHECKSUM)
            R.id.nav_snes_add_del_smc_header -> replaceFragment(NavigateTo.SNES_SMC_HEADER)
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
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(selected: NavigateTo) {
        val fragment: Fragment = when (selected) {
            NavigateTo.APPLY_PATCH -> ApplyPatchFragment()
            NavigateTo.CREATE_PATCH -> CreatePatchFragment()
            NavigateTo.SMD_FIX_CHECKSUM -> SmdFixChecksumFragment()
            NavigateTo.SNES_SMC_HEADER -> SnesSmcHeaderFragment()
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
        Snackbar.make(binding.contentFrame, R.string.main_activity_donate_snackbar_text, Snackbar.LENGTH_INDEFINITE)
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