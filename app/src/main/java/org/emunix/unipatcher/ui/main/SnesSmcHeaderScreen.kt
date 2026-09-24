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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.emunix.unipatcher.MIME_TYPE_ALL_FILES
import org.emunix.unipatcher.MIME_TYPE_OCTET_STREAM
import org.emunix.unipatcher.R
import org.emunix.unipatcher.ui.components.FileSelectCard
import org.emunix.unipatcher.ui.components.InfoCard
import org.emunix.unipatcher.ui.theme.maxContentWidth
import org.emunix.unipatcher.viewmodels.ActionIsRunningViewModel
import org.emunix.unipatcher.viewmodels.SnesSmcHeaderViewModel

@Composable
fun SnesSmcHeaderScreen(
    viewModel: SnesSmcHeaderViewModel,
    actionIsRunningViewModel: ActionIsRunningViewModel,
    registerRunAction: (String, () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val romName by viewModel.romName.collectAsStateWithLifecycle()
    val outputName by viewModel.outputName.collectAsStateWithLifecycle()
    val headerName by viewModel.headerName.collectAsStateWithLifecycle()
    val headerOutputName by viewModel.headerOutputName.collectAsStateWithLifecycle()
    val hasSmcHeader by viewModel.hasSmcHeader.collectAsStateWithLifecycle()
    val suggestedOutputName by viewModel.suggestedOutputName.collectAsStateWithLifecycle()
    val suggestedHeaderOutputName by viewModel.suggestedHeaderOutputName.collectAsStateWithLifecycle()
    val infoText by viewModel.infoText.collectAsStateWithLifecycle()
    val actionIsRunning by viewModel.actionIsRunning.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()

    val addHeaderMode = hasSmcHeader == false

    val context = LocalContext.current

    DisposableEffect(viewModel) {
        registerRunAction(MainRoutes.SNES_SMC_HEADER, viewModel::runActionClicked)
        onDispose { registerRunAction(MainRoutes.SNES_SMC_HEADER, {}) }
    }

    LaunchedEffect(actionIsRunning, status) {
        actionIsRunningViewModel.removeSmc(actionIsRunning)
        actionIsRunningViewModel.setStatus(status)
    }
    LaunchedEffect(viewModel) {
        viewModel.message.collect(actionIsRunningViewModel::setResult)
    }

    val romPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> viewModel.romSelected(uri) }
        }
    }

    val outputPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> viewModel.outputSelected(uri) }
        }
    }

    val headerPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> viewModel.headerFileSelected(uri) }
        }
    }

    val headerOutputPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> viewModel.headerOutputSelected(uri) }
        }
    }

    val selectRom: () -> Unit = {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MIME_TYPE_ALL_FILES
        }
        try {
            romPicker.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_file_picker_app_is_no_installed), Toast.LENGTH_SHORT).show()
        }
    }

    val selectOutput: () -> Unit = {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MIME_TYPE_OCTET_STREAM
            putExtra(Intent.EXTRA_TITLE, suggestedOutputName)
        }
        try {
            outputPicker.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_file_picker_app_is_no_installed), Toast.LENGTH_SHORT).show()
        }
    }

    val selectHeader: () -> Unit = {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MIME_TYPE_ALL_FILES
        }
        try {
            headerPicker.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_file_picker_app_is_no_installed), Toast.LENGTH_SHORT).show()
        }
    }

    val selectHeaderOutput: () -> Unit = {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MIME_TYPE_OCTET_STREAM
            putExtra(Intent.EXTRA_TITLE, suggestedHeaderOutputName)
        }
        try {
            headerOutputPicker.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_file_picker_app_is_no_installed), Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .maxContentWidth()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
        ) {
            FileSelectCard(
                title = stringResource(
                    when (hasSmcHeader) {
                        null -> R.string.main_activity_rom_file
                        false -> R.string.main_activity_rom_without_smc_header
                        true -> R.string.main_activity_rom_with_smc_file
                    }
                ),
                fileName = romName.ifEmpty { stringResource(R.string.main_activity_tap_to_select) },
                onClick = selectRom,
            )

            Spacer(Modifier.height(16.dp))

            InfoCard(
                text = infoText.ifEmpty { stringResource(R.string.snes_smc_header_help) },
            )

            if (hasSmcHeader != null) {
                Spacer(Modifier.height(16.dp))

                FileSelectCard(
                    title = stringResource(
                        if (addHeaderMode) R.string.main_activity_rom_with_header_output
                        else R.string.main_activity_rom_without_smc_file
                    ),
                    fileName = outputName.ifEmpty { stringResource(R.string.main_activity_tap_to_select_where_to_save_rom) },
                    onClick = selectOutput,
                )

                if (addHeaderMode) {
                    Spacer(Modifier.height(16.dp))

                    FileSelectCard(
                        title = stringResource(R.string.main_activity_header_file),
                        fileName = headerName.ifEmpty { stringResource(R.string.main_activity_tap_to_select_optional) },
                        onClick = selectHeader,
                    )
                } else {
                    Spacer(Modifier.height(16.dp))

                    FileSelectCard(
                        title = stringResource(R.string.snes_smc_header_save_title),
                        fileName = headerOutputName.ifEmpty { stringResource(R.string.main_activity_tap_to_select_optional) },
                        onClick = selectHeaderOutput,
                    )
                }
            }
        }
    }
}