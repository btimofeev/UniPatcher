/*
Copyright (C) 2013-2017, 2019-2021 Boris Timofeev

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
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.emunix.unipatcher.R
import org.emunix.unipatcher.Settings
import org.emunix.unipatcher.databinding.ActivityMainBinding
import org.emunix.unipatcher.helpers.SocialHelper
import org.emunix.unipatcher.ui.fragment.*
import org.emunix.unipatcher.viewmodels.ActionIsRunningViewModel
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    enum class NavigateTo {
        APPLY_PATCH, CREATE_PATCH, SMD_FIX_CHECKSUM, SNES_SMC_HEADER
    }

    private val actionIsRunningViewModel by viewModels<ActionIsRunningViewModel>()

    private var actionIsRunning: Boolean = false
    private var doubleBackToExitPressedOnce = false

    private lateinit var _binding: ActivityMainBinding
    private val binding get() = _binding

    @Inject lateinit var social: Lazy<SocialHelper>
    @Inject lateinit var settings : Settings

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
            fragment?.runAction()
        }
        binding.navigationView.setNavigationItemSelectedListener(this)
        if (savedInstanceState == null) {
            replaceFragment(NavigateTo.APPLY_PATCH)
            binding.navigationView.menu.getItem(0).isChecked = true
        }

        actionIsRunningViewModel.get().observe(this, {
            actionIsRunning = it
        })

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
            R.id.nav_rate -> social.get().rateApp()
            R.id.nav_donate -> showDonateActivity()
            R.id.nav_share -> social.get().shareApp()
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
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out)
            replace(R.id.content_frame, fragment)
        }
    }

    override fun onBackPressed() {
        if (!actionIsRunning || doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, getString(R.string.main_activity_double_back_to_exit_message), Toast.LENGTH_SHORT).show()

        GlobalScope.launch {
            delay(2000L)
            doubleBackToExitPressedOnce = false
        }
    }

    private fun showDonateSnackbar() { // don't show snackbar if the user did not patch the file successfully
        if (!settings.getPatchingSuccessful()) return
        // don't show snackbar some time if the user swiped off it before
        var count = settings.getDontShowDonateSnackbarCount()
        if (count != 0) {
            settings.setDontShowDonateSnackbarCount(--count)
            return
        }
        // don't show snackbar each time you open the application
        if (Random().nextInt(6) != 0) return
        Snackbar.make(binding.contentFrame, R.string.main_activity_donate_snackbar_text, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.main_activity_donate_snackbar_button) { showDonateActivity() }
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(snackbar: Snackbar, event: Int) {
                        if (event == DISMISS_EVENT_SWIPE) {
                            settings.setDontShowDonateSnackbarCount(30)
                        }
                    }
                }
                ).show()
    }

    private fun showDonateActivity() {
        val donateIntent = Intent(this, DonateActivity::class.java)
        startActivity(donateIntent)
    }
}