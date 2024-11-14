/*
 Copyright (c) 2017, 2020, 2022, 2024 Boris Timofeev

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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import org.emunix.unipatcher.BuildConfig
import org.emunix.unipatcher.R
import org.emunix.unipatcher.databinding.ActivityDonateBinding

class DonateActivity : AppCompatActivity() {

    private var binding: ActivityDonateBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = ActivityDonateBinding.inflate(layoutInflater)
        binding = layout
        setContentView(layout.root)
        setSupportActionBar(layout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.donate_activity_title)
        layout.sendBitcoinButton.setOnClickListener { donateBitcoin() }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
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

    private fun donateBitcoin() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(BITCOIN_SCHEME + BuildConfig.BITCOIN_ADDRESS)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            copyBitcoinAddressToClipboard()
            binding?.apply {
                bitcoinWalletMessage.isVisible = true
                bitcoinWalletNumber.isVisible = true
                bitcoinWalletNumber.text = BuildConfig.BITCOIN_ADDRESS
            }
        }
    }

    private fun copyBitcoinAddressToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = ClipData.newPlainText(BuildConfig.BITCOIN_ADDRESS, BuildConfig.BITCOIN_ADDRESS)
        clipboard.setPrimaryClip(clip)
    }

    private companion object {

        private const val BITCOIN_SCHEME = "bitcoin:"
    }
}
