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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import kotlinx.android.synthetic.main.activity_help.*
import org.emunix.unipatcher.R
import org.emunix.unipatcher.ui.adapter.HelpPagerAdapter

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.help_activity_faq_tab_title)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.help_activity_about_tab_title)))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter: PagerAdapter = HelpPagerAdapter(supportFragmentManager, tabLayout.tabCount)
        pager.adapter = adapter
        pager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                pager.currentItem = tab.position
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
                sendFeedback()
                true
            }
            R.id.action_visit_website -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_site)))
                startActivity(browserIntent)
                true
            }
            R.id.action_changelog -> {
                val changelogIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/btimofeev/UniPatcher/blob/master/Changelog.md"))
                startActivity(changelogIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sendFeedback() {
        val feedbackIntent = Intent(Intent.ACTION_SEND)
        feedbackIntent.type = "message/rfc822"
        feedbackIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.app_email)))
        feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        try {
            startActivity(Intent.createChooser(feedbackIntent, getString(R.string.send_feedback_dialog_title)))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, R.string.send_feedback_error_no_email_apps, Toast.LENGTH_SHORT).show()
        }
    }
}