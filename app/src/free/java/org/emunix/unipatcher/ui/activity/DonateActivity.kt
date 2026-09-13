package org.emunix.unipatcher.ui.activity

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import org.emunix.unipatcher.BuildConfig
import org.emunix.unipatcher.ui.donate.DonateScreen
import org.emunix.unipatcher.ui.theme.UniPatcherTheme
import org.emunix.unipatcher.utils.enableEdgeToEdgeWithLightStatusBar

class DonateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdgeWithLightStatusBar()
        super.onCreate(savedInstanceState)

        setContent {
            UniPatcherTheme {
                var showWalletInfo by remember { mutableStateOf(false) }
                DonateScreen(
                    bitcoinAddress = BuildConfig.BITCOIN_ADDRESS,
                    showWalletInfo = showWalletInfo,
                    onBitcoinClick = { donateBitcoin { showWalletInfo = true } },
                    onBackPressed = { finish() },
                )
            }
        }
    }

    private fun donateBitcoin(onWalletInfoShown: () -> Unit) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = (BITCOIN_SCHEME + BuildConfig.BITCOIN_ADDRESS).toUri()
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            copyBitcoinAddressToClipboard()
            onWalletInfoShown()
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