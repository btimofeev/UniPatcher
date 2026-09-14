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

package org.emunix.unipatcher.ui.help

import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.launch
import org.emunix.unipatcher.R
import org.emunix.unipatcher.ui.model.FaqItem
import org.emunix.unipatcher.ui.theme.maxContentWidth
import org.emunix.unipatcher.ui.util.resourceText

private const val TABS_COUNT = 2

@Composable
fun HelpScreen(
    appVersion: String,
    onBackPressed: () -> Unit,
    onVisitSiteClick: () -> Unit,
    onChangelogClick: () -> Unit,
) {
    HelpContent(
        appVersion = appVersion,
        onBackPressed = onBackPressed,
        onVisitSiteClick = onVisitSiteClick,
        onChangelogClick = onChangelogClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HelpContent(
    appVersion: String,
    onBackPressed: () -> Unit,
    onVisitSiteClick: () -> Unit,
    onChangelogClick: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = 0) { TABS_COUNT }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.help_activity_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    },
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    TabRow(
                        modifier = Modifier.maxContentWidth(),
                        selectedTabIndex = pagerState.currentPage,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        },
                    ) {
                        Tab(
                            selected = pagerState.currentPage == TAB_FAQ,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(TAB_FAQ) }
                            },
                            text = { Text(stringResource(R.string.help_activity_faq_tab_title)) },
                        )
                        Tab(
                            selected = pagerState.currentPage == TAB_ABOUT,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(TAB_ABOUT) }
                            },
                            text = { Text(stringResource(R.string.help_activity_about_tab_title)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .maxContentWidth()
                    .fillMaxSize(),
            ) { page ->
                when (page) {
                    TAB_FAQ -> FaqTab()
                    TAB_ABOUT -> AboutTab(
                        appVersion = appVersion,
                        onVisitSiteClick = onVisitSiteClick,
                        onChangelogClick = onChangelogClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun FaqTab() {
    val faqItems = listOf(
        FaqItem(R.string.help_what_is_unipatcher_title, R.string.help_what_is_unipatcher_body),
        FaqItem(R.string.help_what_patch_formats_are_supported_title, R.string.help_what_patch_formats_are_supported_body),
        FaqItem(R.string.help_can_i_hack_android_game_title, R.string.help_can_i_hack_android_game_body),
        FaqItem(R.string.help_what_is_rom_image_title, R.string.help_what_is_rom_image_body),
        FaqItem(R.string.help_what_is_rom_hacking_title, R.string.help_what_is_rom_hacking_body),
        FaqItem(R.string.help_what_is_a_patch_title, R.string.help_what_is_a_patch_body),
        FaqItem(R.string.help_why_not_distribute_modified_games_title, R.string.help_why_not_distribute_modified_games_body),
        FaqItem(R.string.help_how_to_apply_patch_to_rom_title, R.string.help_how_to_apply_patch_to_rom_body),
        FaqItem(R.string.help_archive_should_be_unpacked_title, R.string.help_archive_should_be_unpacked_body),
        FaqItem(R.string.help_rom_not_compatible_with_patch_title, R.string.help_rom_not_compatible_with_patch_body),
        FaqItem(R.string.help_i_cant_patch_smw_rom_title, R.string.help_i_cant_patch_smw_rom_body),
        FaqItem(R.string.help_find_rom_pokemon_emerald_title, R.string.help_find_rom_pokemon_emerald_body),
        FaqItem(R.string.help_ips_no_checksum_title, R.string.help_ips_no_checksum_body),
        FaqItem(R.string.help_ecm_image_title, R.string.help_ecm_image_body),
        FaqItem(R.string.help_additional_features_title, R.string.help_additional_features_body),
        FaqItem(R.string.help_sega_fix_checksum_title, R.string.help_sega_fix_checksum_body),
        FaqItem(R.string.help_snes_smc_title, R.string.help_snes_smc_body),
        FaqItem(R.string.help_how_to_translate_title, R.string.help_how_to_translate_body),
        FaqItem(R.string.help_feedback_title, R.string.help_feedback_body),
    )

    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.help_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
            faqItems.forEach { item ->
                Text(
                    text = stringResource(item.title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                )
                Text(
                    text = resourceText(item.body),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun AboutTab(
    appVersion: String,
    onVisitSiteClick: () -> Unit,
    onChangelogClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        SelectionContainer {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                val context = LocalContext.current
                val launcherBitmap = remember(context) {
                    val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
                    drawable?.let {
                        val bitmap = createBitmap(it.intrinsicWidth, it.intrinsicHeight)
                        it.setBounds(0, 0, bitmap.width, bitmap.height)
                        it.draw(Canvas(bitmap))
                        bitmap
                    }
                }
                if (launcherBitmap != null) {
                    Image(
                        bitmap = launcherBitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(64.dp),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.help_activity_about_tab_version, appVersion),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.help_activity_about_tab_license),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.help_activity_about_tab_copyright),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        Row(modifier = Modifier.padding(top = 16.dp)) {
            OutlinedButton(
                onClick = onVisitSiteClick,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.help_activity_action_visit_site),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onChangelogClick,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.help_activity_action_changelog),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        SelectionContainer {
            Column {
                    Text(
                        text = resourceText(R.string.about_translators),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Spacer(Modifier.height(48.dp))

                    Text(
                        text = resourceText(R.string.about_used_libraries),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 24.dp),
                    )
            }
        }
    }
}

private const val TAB_FAQ = 0
private const val TAB_ABOUT = 1