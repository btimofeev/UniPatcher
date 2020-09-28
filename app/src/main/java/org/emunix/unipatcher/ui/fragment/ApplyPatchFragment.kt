/*
Copyright (C) 2014, 2016, 2017, 2019-2020 Boris Timofeev

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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import org.apache.commons.io.FilenameUtils
import org.emunix.unipatcher.*
import org.emunix.unipatcher.Utils.startForegroundService
import org.emunix.unipatcher.databinding.ApplyPatchFragmentBinding
import org.emunix.unipatcher.ui.activity.HelpActivity
import timber.log.Timber

class ApplyPatchFragment : ActionFragment(), View.OnClickListener {

    private var romPath: String = ""     //
    private var patchPath: String = ""   // String representation of Uri, example "content://com.app.name/path_to_file
    private var outputPath: String = ""  //

    private var _binding: ApplyPatchFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ApplyPatchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        UniPatcher.appComponent.inject(this)
        activity?.setTitle(R.string.nav_apply_patch)
        binding.patchCardView.setOnClickListener(this)
        binding.romCardView.setOnClickListener(this)
        binding.outputCardView.setOnClickListener(this)
        binding.howToUseAppButton.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (Settings.getShowHelpButton())
            binding.howToUseAppButton.visibility = View.VISIBLE
        else
            binding.howToUseAppButton.visibility = View.GONE
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            romPath = savedInstanceState.getString("romPath") ?: ""
            patchPath = savedInstanceState.getString("patchPath") ?: ""
            outputPath = savedInstanceState.getString("outputPath") ?: ""
            if (romPath.isNotEmpty())
                binding.romNameTextView.text = DocumentFile.fromSingleUri(requireContext(), Uri.parse(romPath))?.name ?: "unknown"
            if (patchPath.isNotEmpty())
                binding.patchNameTextView.text = DocumentFile.fromSingleUri(requireContext(), Uri.parse(patchPath))?.name ?: "unknown"
            if (outputPath.isNotEmpty())
                binding.outputNameTextView.text = DocumentFile.fromSingleUri(requireContext(), Uri.parse(outputPath))?.name ?: "unknown"
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("romPath", romPath)
        savedInstanceState.putString("patchPath", patchPath)
        savedInstanceState.putString("outputPath", outputPath)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.patchCardView -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                try {
                    startActivityForResult(intent, Action.SELECT_PATCH_FILE)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), R.string.error_file_picker_app_is_no_installed, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.romCardView -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                try {
                    startActivityForResult(intent, Action.SELECT_ROM_FILE)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), R.string.error_file_picker_app_is_no_installed, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.outputCardView -> {
                var title = "specify_rom_name"
                if (romPath.isNotBlank()) {
                    val romName = DocumentFile.fromSingleUri(requireContext(), Uri.parse(romPath))?.name
                    if (romName != null)
                        title = makeOutputTitle(romName)
                }
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_TITLE, title)
                }
                try {
                    startActivityForResult(intent, Action.SELECT_OUTPUT_FILE)
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), R.string.error_file_picker_app_is_no_installed, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.howToUseAppButton -> {
                val helpIntent = Intent(requireContext(), HelpActivity::class.java)
                startActivity(helpIntent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        Timber.d("onActivityResult($requestCode, $resultCode, $resultData)")
        if (resultCode == Activity.RESULT_OK && resultData != null && (requestCode == Action.SELECT_PATCH_FILE || requestCode == Action.SELECT_ROM_FILE || requestCode == Action.SELECT_OUTPUT_FILE)) {
            resultData.data?.let { uri ->
                Timber.d(uri.toString())
                val takeFlags = resultData.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
                DocumentFile.fromSingleUri(requireContext(), uri)?.name?.let { fileName ->
                    when (requestCode) {
                        Action.SELECT_PATCH_FILE -> {
                            patchPath = uri.toString()
                            binding.patchNameTextView.visibility = View.VISIBLE
                            binding.patchNameTextView.text = fileName
                            checkArchive(fileName)
                        }
                        Action.SELECT_ROM_FILE -> {
                            romPath = uri.toString()
                            binding.romNameTextView.visibility = View.VISIBLE
                            binding.romNameTextView.text = fileName
                            checkArchive(fileName)
                        }
                        Action.SELECT_OUTPUT_FILE -> {
                            outputPath = uri.toString()
                            binding.outputNameTextView.visibility = View.VISIBLE
                            binding.outputNameTextView.text = fileName
                        }
                    }
                }
            }
            super.onActivityResult(requestCode, resultCode, resultData)
        }
    }

    private fun makeOutputTitle(romName: String): String {
        val baseName = FilenameUtils.getBaseName(romName)
        val ext = FilenameUtils.getExtension(romName)
        return "$baseName [patched].$ext"
    }

    override fun runAction(): Boolean {
        when {
            romPath.isEmpty() -> {
                Toast.makeText(activity, getString(R.string.main_activity_toast_rom_not_selected), Toast.LENGTH_LONG).show()
                return false
            }
            patchPath.isEmpty() -> {
                Toast.makeText(activity, getString(R.string.main_activity_toast_patch_not_selected), Toast.LENGTH_LONG).show()
                return false
            }
            outputPath.isEmpty() -> {
                Toast.makeText(activity, getString(R.string.main_activity_toast_output_not_selected), Toast.LENGTH_LONG).show()
                return false
            }
            else -> {
                val rom = DocumentFile.fromSingleUri(requireContext(), Uri.parse(romPath))
                val patch = DocumentFile.fromSingleUri(requireContext(), Uri.parse(patchPath))
                val intent = Intent(activity, WorkerService::class.java)
                intent.putExtra("action", Action.APPLY_PATCH)
                intent.putExtra("romPath", romPath)
                intent.putExtra("patchPath", patchPath)
                intent.putExtra("outputPath", outputPath)
                intent.putExtra("romName", rom?.name ?: "")
                intent.putExtra("patchName", patch?.name ?: "undefined")
                startForegroundService(requireActivity(), intent)
                Toast.makeText(activity, R.string.toast_patching_started_check_notify, Toast.LENGTH_SHORT).show()
                return true
            }
        }
    }

    private fun checkArchive(fileName: String) {
        if (Utils.isArchive(fileName))
            Toast.makeText(requireContext(), R.string.main_activity_toast_archives_not_supported, Toast.LENGTH_LONG).show()
    }
}