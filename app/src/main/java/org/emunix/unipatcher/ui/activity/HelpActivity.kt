/*
Copyright (C) 2016, 2019-2021, 2026 Boris Timofeev

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
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.unipatcher.helpers.ResourceProvider
import org.emunix.unipatcher.helpers.SocialHelper
import org.emunix.unipatcher.ui.help.HelpScreen
import org.emunix.unipatcher.ui.theme.UniPatcherTheme
import org.emunix.unipatcher.utils.enableEdgeToEdgeWithLightStatusBar
import javax.inject.Inject

@AndroidEntryPoint
class HelpActivity : AppCompatActivity() {

    @Inject
    lateinit var social: Lazy<SocialHelper>

    @Inject
    lateinit var resourceProvider: ResourceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdgeWithLightStatusBar()
        super.onCreate(savedInstanceState)

        setContent {
            UniPatcherTheme {
                HelpScreen(
                    appVersion = resourceProvider.appVersion,
                    onBackPressed = { finish() },
                    onVisitSiteClick = { social.get().openWebsite() },
                    onChangelogClick = { social.get().showChangelog() },
                )
            }
        }
    }
}