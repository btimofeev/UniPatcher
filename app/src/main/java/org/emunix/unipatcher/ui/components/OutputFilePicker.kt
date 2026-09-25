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

package org.emunix.unipatcher.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.documentfile.provider.DocumentFile
import org.emunix.unipatcher.R

/**
 * Button for choosing where to save the result file.
 *
 * Uses the system "Save as" dialog ([Intent.ACTION_CREATE_DOCUMENT]) when
 * available. On devices without a DocumentsProvider that handles it,
 * falls back to choosing a folder ([Intent.ACTION_OPEN_DOCUMENT_TREE]) and
 * asks for the file name in-app before creating the file.
 */
@Composable
fun rememberOutputFilePicker(
    suggestedName: String,
    mimeType: String,
    onFileCreated: (Uri) -> Unit,
): () -> Unit {
    val context = LocalContext.current

    var showNameDialog by remember { mutableStateOf(false) }
    var pendingTreeUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let(onFileCreated)
    }

    val openTreeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            } catch (_: Exception) {
            }
            pendingTreeUri = treeUri
            fileName = suggestedName
            showNameDialog = true
        }
    }

    fun createFileInTree() {
        val treeUri = pendingTreeUri ?: return
        val cleanName = sanitizeFileName(fileName).ifEmpty { suggestedName }
        try {
            DocumentFile.fromTreeUri(context, treeUri)
                ?.createFile(mimeType, cleanName)
                ?.uri
                ?.let(onFileCreated)
        } catch (_: Exception) {
        } finally {
            pendingTreeUri = null
            showNameDialog = false
        }
    }

    val launch: () -> Unit = {
        val createIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, suggestedName)
        }
        try {
            createDocumentLauncher.launch(createIntent)
        } catch (e: ActivityNotFoundException) {
            try {
                openTreeLauncher.launch(null)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_file_picker_app_is_no_installed),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = {
                showNameDialog = false
                pendingTreeUri = null
            },
            title = { Text(stringResource(R.string.output_file_name_title)) },
            text = {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = { createFileInTree() }) {
                    Text(stringResource(R.string.output_file_name_save))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNameDialog = false
                    pendingTreeUri = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    return launch
}

private fun sanitizeFileName(name: String): String =
    name.replace(Regex("[\\\\/:*?\"<>|\u0000-\u001f]"), "")
        .trim()
        .trimEnd('.')