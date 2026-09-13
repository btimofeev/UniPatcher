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

package org.emunix.unipatcher.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.emunix.unipatcher.R
import org.emunix.unipatcher.ui.theme.maxContentWidth
import org.emunix.unipatcher.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackPressed: () -> Unit,
) {
    val showHelpButton by viewModel.showHelpButton.collectAsStateWithLifecycle()
    val ignoreChecksum by viewModel.ignoreChecksum.collectAsStateWithLifecycle()
    val theme by viewModel.theme.collectAsStateWithLifecycle()

    val themeNames = stringArrayResource(R.array.theme_names)
    val themeValues = stringArrayResource(R.array.theme_values)

    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_activity_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .maxContentWidth()
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                CategoryHeader(stringResource(R.string.settings_interface_header))

                SettingsListItem(
                    icon = { Icon(painterResource(R.drawable.ic_brightness_medium), contentDescription = null) },
                    title = stringResource(R.string.settings_theme),
                    summary = themeNames.getOrElse(themeValues.indexOf(theme)) { "" },
                    onClick = { showThemeDialog = true },
                )

                SettingsSwitchItem(
                    icon = { Icon(painterResource(R.drawable.ic_book), contentDescription = null) },
                    title = stringResource(R.string.settings_show_button_how_to_use_app),
                    summary = stringResource(R.string.settings_show_button_how_to_use_app_description),
                    checked = showHelpButton,
                    onCheckedChange = viewModel::setShowHelpButton,
                )

                CategoryHeader(stringResource(R.string.settings_patching_header))

                SettingsSwitchItem(
                    icon = { Icon(painterResource(R.drawable.ic_fingerprint), contentDescription = null) },
                    title = stringResource(R.string.settings_ignore_checksum),
                    summary = stringResource(R.string.settings_ignore_checksum_description),
                    checked = ignoreChecksum,
                    onCheckedChange = viewModel::setIgnoreChecksum,
                )
            }
        }
    }

    if (showThemeDialog) {
        ThemeDialog(
            themeNames = themeNames,
            themeValues = themeValues,
            selectedTheme = theme,
            onThemeSelected = { value ->
                showThemeDialog = false
                viewModel.setTheme(value)
            },
            onDismiss = { showThemeDialog = false },
        )
    }
}

@Composable
private fun CategoryHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
    )
    HorizontalDivider()
}

@Composable
private fun SettingsListItem(
    icon: @Composable () -> Unit,
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(modifier = Modifier.width(40.dp)) {
            icon()
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: @Composable () -> Unit,
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(modifier = Modifier.width(40.dp)) {
            icon()
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun ThemeDialog(
    themeNames: Array<String>,
    themeValues: Array<String>,
    selectedTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme_dialog_title)) },
        text = {
            Column {
                themeValues.forEachIndexed { index, value ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(value) }
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = value == selectedTheme,
                            onClick = null,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = themeNames[index],
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
    )
}