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
import org.emunix.unipatcher.ui.theme.maxContentWidth
import org.emunix.unipatcher.viewmodels.ActionIsRunningViewModel
import org.emunix.unipatcher.viewmodels.CreatePatchViewModel

@Composable
fun CreatePatchScreen(
    viewModel: CreatePatchViewModel,
    actionIsRunningViewModel: ActionIsRunningViewModel,
    registerRunAction: (String, () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sourceName by viewModel.sourceName.collectAsStateWithLifecycle()
    val modifiedName by viewModel.modifiedName.collectAsStateWithLifecycle()
    val patchName by viewModel.patchName.collectAsStateWithLifecycle()
    val actionIsRunning by viewModel.actionIsRunning.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()

    val context = LocalContext.current

    DisposableEffect(viewModel) {
        registerRunAction(MainRoutes.CREATE_PATCH, viewModel::runActionClicked)
        onDispose { registerRunAction(MainRoutes.CREATE_PATCH, {}) }
    }

    LaunchedEffect(actionIsRunning, status) {
        actionIsRunningViewModel.createPatch(actionIsRunning)
        actionIsRunningViewModel.setStatus(status)
    }
    LaunchedEffect(viewModel) {
        viewModel.message.collect(actionIsRunningViewModel::setResult)
    }

    val sourcePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> viewModel.sourceSelected(uri) }
        }
    }

    val modifiedPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> viewModel.modifiedSelected(uri) }
        }
    }

    val patchPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> viewModel.patchSelected(uri) }
        }
    }

    val pickFile: ((Intent) -> Unit) -> Unit = { launch ->
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MIME_TYPE_ALL_FILES
        }
        try {
            launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_file_picker_app_is_no_installed), Toast.LENGTH_SHORT).show()
        }
    }

    val launchCreateDocument: () -> Unit = {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MIME_TYPE_OCTET_STREAM
            putExtra(Intent.EXTRA_TITLE, "patch.xdelta")
        }
        try {
            patchPicker.launch(intent)
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
                title = stringResource(R.string.create_patch_fragment_source_file),
                fileName = sourceName.ifEmpty { stringResource(R.string.main_activity_tap_to_select) },
                onClick = { pickFile(sourcePicker::launch) },
            )

            Spacer(Modifier.height(16.dp))

            FileSelectCard(
                title = stringResource(R.string.create_patch_fragment_modified_file),
                fileName = modifiedName.ifEmpty { stringResource(R.string.main_activity_tap_to_select) },
                onClick = { pickFile(modifiedPicker::launch) },
            )

            Spacer(Modifier.height(16.dp))

            FileSelectCard(
                title = stringResource(R.string.create_patch_fragment_patch_file),
                fileName = patchName.ifEmpty { stringResource(R.string.main_activity_tap_to_select_where_to_save_patch) },
                onClick = launchCreateDocument,
            )
        }
    }
}