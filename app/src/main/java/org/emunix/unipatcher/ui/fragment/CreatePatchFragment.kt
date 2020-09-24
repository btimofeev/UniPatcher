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
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import org.emunix.unipatcher.*
import org.emunix.unipatcher.Utils.startForegroundService
import org.emunix.unipatcher.databinding.CreatePatchFragmentBinding
import timber.log.Timber

class CreatePatchFragment : ActionFragment(), View.OnClickListener {

    private var sourcePath: String = ""   //
    private var modifiedPath: String = "" // String representation of Uri, example "content://com.app.name/path_to_file
    private var patchPath: String = ""    //

    private var _binding: CreatePatchFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = CreatePatchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        UniPatcher.appComponent.inject(this)
        activity?.setTitle(R.string.nav_create_patch)
        binding.sourceFileCardView.setOnClickListener(this)
        binding.modifiedFileCardView.setOnClickListener(this)
        binding.patchFileCardView.setOnClickListener(this)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            sourcePath = savedInstanceState.getString("sourcePath") ?: ""
            modifiedPath = savedInstanceState.getString("modifiedPath") ?: ""
            patchPath = savedInstanceState.getString("patchPath") ?: ""
            if (sourcePath.isNotEmpty())
                binding.sourceFileNameTextView.text = DocumentFile.fromSingleUri(requireContext(), Uri.parse(sourcePath))?.name ?: "unknown"
            if (modifiedPath.isNotEmpty())
                binding.modifiedFileNameTextView.text = DocumentFile.fromSingleUri(requireContext(), Uri.parse(modifiedPath))?.name ?: "unknown"
            if (patchPath.isNotEmpty())
                binding.patchFileNameTextView.text = DocumentFile.fromSingleUri(requireContext(), Uri.parse(patchPath))?.name ?: "unknown"
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("sourcePath", sourcePath)
        savedInstanceState.putString("modifiedPath", modifiedPath)
        savedInstanceState.putString("patchPath", patchPath)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.sourceFileCardView -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(intent, Action.SELECT_SOURCE_FILE)
            }
            R.id.modifiedFileCardView -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(intent, Action.SELECT_MODIFIED_FILE)
            }
            R.id.patchFileCardView -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_TITLE, "patch.xdelta")
                }
                startActivityForResult(intent, Action.SELECT_PATCH_FILE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        Timber.d("onActivityResult($requestCode, $resultCode, $resultData)")
        if (resultCode == Activity.RESULT_OK && resultData != null && (requestCode == Action.SELECT_SOURCE_FILE || requestCode == Action.SELECT_MODIFIED_FILE || requestCode == Action.SELECT_PATCH_FILE)) {
            resultData.data?.let { uri ->
                Timber.d(uri.toString())
                val takeFlags = resultData.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
                DocumentFile.fromSingleUri(requireContext(), uri)?.name?.let { fileName ->
                    when (requestCode) {
                        Action.SELECT_SOURCE_FILE -> {
                            sourcePath = uri.toString()
                            binding.sourceFileNameTextView.visibility = View.VISIBLE
                            binding.sourceFileNameTextView.text = fileName
                        }
                        Action.SELECT_MODIFIED_FILE -> {
                            modifiedPath = uri.toString()
                            binding.modifiedFileNameTextView.visibility = View.VISIBLE
                            binding.modifiedFileNameTextView.text = fileName
                        }
                        Action.SELECT_PATCH_FILE -> {
                            patchPath = uri.toString()
                            binding.patchFileNameTextView.visibility = View.VISIBLE
                            binding.patchFileNameTextView.text = fileName
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    override fun runAction(): Boolean {
        when {
            sourcePath.isEmpty() -> {
                Toast.makeText(activity, getString(R.string.create_patch_fragment_toast_source_not_selected), Toast.LENGTH_LONG).show()
                return false
            }
            modifiedPath.isEmpty() -> {
                Toast.makeText(activity, getString(R.string.create_patch_fragment_toast_modified_not_selected), Toast.LENGTH_LONG).show()
                return false
            }
            patchPath.isEmpty() -> {
                Toast.makeText(activity, getString(R.string.create_patch_fragment_toast_patch_not_selected), Toast.LENGTH_LONG).show()
                return false
            }
            else -> {
                val patchName = DocumentFile.fromSingleUri(requireContext(), Uri.parse(patchPath))?.name ?: ""
                val intent = Intent(activity, WorkerService::class.java)
                intent.putExtra("action", Action.CREATE_PATCH)
                intent.putExtra("sourcePath", sourcePath)
                intent.putExtra("modifiedPath", modifiedPath)
                intent.putExtra("patchPath", patchPath)
                intent.putExtra("patchName", patchName)
                startForegroundService(requireActivity(), intent)
                Toast.makeText(activity, R.string.toast_create_patch_started_check_notify, Toast.LENGTH_SHORT).show()
                return true
            }
        }
    }
}