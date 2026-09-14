/*
 Copyright (c) 2026 Boris Timofeev

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

package org.emunix.unipatcher.ui.main

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import org.emunix.unipatcher.BuildConfig
import org.emunix.unipatcher.ui.donate.DonateScreen

@Composable
fun DonateScreenRoute(
    onBackPressed: () -> Unit,
) {
    val context = LocalContext.current
    var showWalletInfo by remember { mutableStateOf(false) }

    DonateScreen(
        bitcoinAddress = BuildConfig.BITCOIN_ADDRESS,
        showWalletInfo = showWalletInfo,
        onBitcoinClick = {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = (BITCOIN_SCHEME + BuildConfig.BITCOIN_ADDRESS).toUri()
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                copyBitcoinAddressToClipboard(context)
                showWalletInfo = true
            }
        },
        onBackPressed = onBackPressed,
    )
}

private fun copyBitcoinAddressToClipboard(context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    val clip = ClipData.newPlainText(BuildConfig.BITCOIN_ADDRESS, BuildConfig.BITCOIN_ADDRESS)
    clipboard.setPrimaryClip(clip)
}

private const val BITCOIN_SCHEME = "bitcoin:"