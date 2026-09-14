/*
Copyright (C) 2013-2017, 2019-2021, 2024, 2026 Boris Timofeev

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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.unipatcher.Settings
import org.emunix.unipatcher.helpers.ResourceProvider
import org.emunix.unipatcher.helpers.SocialHelper
import org.emunix.unipatcher.helpers.ThemeHelper
import org.emunix.unipatcher.ui.main.MainScreen
import org.emunix.unipatcher.ui.theme.UniPatcherTheme
import org.emunix.unipatcher.utils.enableEdgeToEdgeWithLightStatusBar
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var social: Lazy<SocialHelper>
    @Inject
    lateinit var settings: Settings
    @Inject
    lateinit var resourceProvider: ResourceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdgeWithLightStatusBar()
        super.onCreate(savedInstanceState)

        setContent {
            val theme by settings.getThemeFlow().collectAsStateWithLifecycle()
            val darkTheme = when (theme) {
                ThemeHelper.LIGHT_MODE -> false
                ThemeHelper.DARK_MODE -> true
                else -> isSystemInDarkTheme()
            }
            UniPatcherTheme(darkTheme = darkTheme) {
                MainScreen(
                    settings = settings,
                    appVersion = resourceProvider.appVersion,
                    onVisitSiteClick = { social.get().openWebsite() },
                    onChangelogClick = { social.get().showChangelog() },
                    onRate = { social.get().rateApp() },
                    onShare = { social.get().shareApp() },
                )
            }
        }
    }
}