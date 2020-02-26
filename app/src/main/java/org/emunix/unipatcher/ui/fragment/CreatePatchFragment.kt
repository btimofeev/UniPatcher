/*
 Copyright (C) 2017, 2020 Boris Timofeev

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
package org.emunix.unipatcher.ui.fragment

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.create_patch_fragment.*
import org.apache.commons.io.FilenameUtils
import org.emunix.unipatcher.Action
import org.emunix.unipatcher.R
import org.emunix.unipatcher.Settings.getOutputDir
import org.emunix.unipatcher.Settings.getRomDir
import org.emunix.unipatcher.Settings.setLastRomDir
import org.emunix.unipatcher.Utils.dpToPx
import org.emunix.unipatcher.Utils.startForegroundService
import org.emunix.unipatcher.WorkerService
import org.emunix.unipatcher.ui.activity.FilePickerActivity
import timber.log.Timber
import java.io.File

class CreatePatchFragment : ActionFragment(), View.OnClickListener {

    private var sourcePath: String = ""
    private var modifiedPath: String = ""
    private var patchPath: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.create_patch_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.setTitle(R.string.nav_create_patch)
        sourceFileCardView.setOnClickListener(this)
        modifiedFileCardView.setOnClickListener(this)
        patchFileCardView.setOnClickListener(this)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            sourcePath = savedInstanceState.getString("sourcePath") ?: ""
            modifiedPath = savedInstanceState.getString("modifiedPath") ?: ""
            patchPath = savedInstanceState.getString("patchPath") ?: ""
            if (sourcePath.isNotEmpty()) sourceFileNameTextView.text = File(sourcePath).name
            if (modifiedPath.isNotEmpty()) modifiedFileNameTextView.text = File(modifiedPath).name
            if (patchPath.isNotEmpty()) patchFileNameTextView.text = File(patchPath).name
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("sourcePath", sourcePath)
        savedInstanceState.putString("modifiedPath", modifiedPath)
        savedInstanceState.putString("patchPath", patchPath)
    }

    override fun onClick(view: View) {
        val intent = Intent(activity, FilePickerActivity::class.java)
        when (view.id) {
            R.id.sourceFileCardView -> {
                intent.putExtra("title", getString(R.string.file_picker_activity_title_select_source_file))
                intent.putExtra("directory", getRomDir(activity!!))
                startActivityForResult(intent, Action.SELECT_SOURCE_FILE)
            }
            R.id.modifiedFileCardView -> {
                intent.putExtra("title", getString(R.string.file_picker_activity_title_select_modified_file))
                intent.putExtra("directory", getRomDir(activity!!))
                startActivityForResult(intent, Action.SELECT_MODIFIED_FILE)
            }
            R.id.patchFileCardView -> renamePatchFile()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult($requestCode, $resultCode, $data)")
        if (resultCode == Activity.RESULT_OK) {
            val path = data?.getStringExtra("path") ?: ""
            if (path.isBlank()) {
                Toast.makeText(activity, R.string.main_activity_toast_file_manager_did_not_return_file_path, Toast.LENGTH_LONG).show()
                return
            }
            val filePath = File(path)
            val dir = filePath.parent
            when (requestCode) {
                Action.SELECT_SOURCE_FILE -> {
                    sourcePath = path
                    sourceFileNameTextView.visibility = View.VISIBLE
                    sourceFileNameTextView.text = filePath.name
                    if (dir != null) {
                        setLastRomDir(activity!!, dir)
                    }
                }
                Action.SELECT_MODIFIED_FILE -> {
                    modifiedPath = path
                    modifiedFileNameTextView.visibility = View.VISIBLE
                    modifiedFileNameTextView.text = filePath.name
                    if (dir != null) {
                        setLastRomDir(activity!!, dir)
                    }
                    patchPath = makeOutputPath(path)
                    patchFileNameTextView!!.text = File(patchPath).name
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun makeOutputPath(fullname: String): String {
        var dir = getOutputDir(activity!!)
        if (dir == "") { // get ROM directory
            dir = FilenameUtils.getFullPath(fullname)
        }
        val baseName = FilenameUtils.getBaseName(fullname)
        return FilenameUtils.concat(dir, "$baseName.xdelta")
    }

    override fun runAction(): Boolean {
        when {
            sourcePath.isEmpty() && modifiedPath.isEmpty() -> {
                Toast.makeText(activity, getString(R.string.create_patch_fragment_toast_source_and_modified_not_selected), Toast.LENGTH_LONG).show()
                return false
            }
            sourcePath.isEmpty() -> {
                Toast.makeText(activity, getString(R.string.create_patch_fragment_toast_source_not_selected), Toast.LENGTH_LONG).show()
                return false
            }
            modifiedPath.isEmpty() -> {
                Toast.makeText(activity, getString(R.string.create_patch_fragment_toast_modified_not_selected), Toast.LENGTH_LONG).show()
                return false
            }
            else -> {
                val intent = Intent(activity, WorkerService::class.java)
                intent.putExtra("action", Action.CREATE_PATCH)
                intent.putExtra("sourcePath", sourcePath)
                intent.putExtra("modifiedPath", modifiedPath)
                intent.putExtra("patchPath", patchPath)
                startForegroundService(activity!!, intent)
                Toast.makeText(activity, R.string.toast_create_patch_started_check_notify, Toast.LENGTH_SHORT).show()
                return true
            }
        }
    }

    private fun renamePatchFile() {
        if (modifiedPath.isEmpty()) {
            Toast.makeText(activity, getString(R.string.create_patch_fragment_toast_modified_not_selected), Toast.LENGTH_LONG).show()
            return
        }
        val renameDialog = AlertDialog.Builder(activity!!)
        renameDialog.setTitle(R.string.dialog_rename_title)
        val input = EditText(activity)
        input.setText(patchFileNameTextView!!.text)
        // add left and right margins to EditText.
        val container = FrameLayout(activity!!)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val dp24 = dpToPx(activity!!, 24)
        params.setMargins(dp24, 0, dp24, 0)
        input.layoutParams = params
        container.addView(input)
        renameDialog.setView(container)
        renameDialog.setPositiveButton(R.string.dialog_rename_ok, DialogInterface.OnClickListener { _, _ ->
            var newName = input.text.toString()
            if (newName == "") {
                Toast.makeText(activity, R.string.dialog_rename_error_empty_name, Toast.LENGTH_LONG).show()
                return@OnClickListener
            }
            if (newName.contains("/")) {
                newName = newName.replace("/".toRegex(), "_")
                Toast.makeText(activity, R.string.dialog_rename_error_invalid_chars, Toast.LENGTH_LONG).show()
            }
            val newPath = File(patchPath).parent + File.separator + newName
            if (FilenameUtils.equals(newPath, sourcePath) || FilenameUtils.equals(newPath, modifiedPath)) {
                Toast.makeText(activity, R.string.dialog_rename_error_same_name, Toast.LENGTH_LONG).show()
                return@OnClickListener
            }
            patchFileNameTextView.text = newName
            patchPath = newPath
        })
        renameDialog.setNegativeButton(R.string.dialog_rename_cancel) { dialog, _ -> dialog.cancel() }
        renameDialog.show()
    }
}