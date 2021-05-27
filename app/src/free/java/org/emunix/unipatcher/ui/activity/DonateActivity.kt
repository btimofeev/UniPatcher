/*
 Copyright (c) 2017, 2020 Boris Timofeev

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
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import org.emunix.unipatcher.BuildConfig
import org.emunix.unipatcher.R
import org.emunix.unipatcher.databinding.ActivityDonateBinding
import org.sufficientlysecure.donations.DonationsFragment

class DonateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDonateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.donate_activity_title)

        val fragment = DonationsFragment.Companion.newInstance(
            BuildConfig.DEBUG, false, null,
            null, null,
            true, BuildConfig.PAYPAL_USER, BuildConfig.PAYPAL_CURRENCY_CODE,
            getString(R.string.donation), true, BuildConfig.BITCOIN_ADDRESS
        )
        supportFragmentManager.commit {
            replace(R.id.donate_fragment, fragment)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
