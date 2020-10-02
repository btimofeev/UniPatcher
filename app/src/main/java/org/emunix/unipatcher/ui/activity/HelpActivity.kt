/*
Copyright (C) 2016, 2019-2020 Boris Timofeev

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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import dagger.Lazy
import org.emunix.unipatcher.R
import org.emunix.unipatcher.UniPatcher
import org.emunix.unipatcher.databinding.ActivityHelpBinding
import org.emunix.unipatcher.helpers.SocialHelper
import org.emunix.unipatcher.ui.adapter.HelpPagerAdapter
import javax.inject.Inject

class HelpActivity : AppCompatActivity() {

    @Inject lateinit var social: Lazy<SocialHelper>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UniPatcher.appComponent.inject(this)

        val binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.help_activity_faq_tab_title)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.help_activity_about_tab_title)))
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter: PagerAdapter = HelpPagerAdapter(supportFragmentManager, binding.tabLayout.tabCount)
        binding.pager.adapter = adapter
        binding.pager.addOnPageChangeListener(TabLayoutOnPageChangeListener(binding.tabLayout))
        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.pager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_help, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_send_feedback -> {
                social.get().sendFeedback()
                true
            }
            R.id.action_visit_website -> {
                social.get().openWebsite()
                true
            }
            R.id.action_changelog -> {
                social.get().showChangelog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}