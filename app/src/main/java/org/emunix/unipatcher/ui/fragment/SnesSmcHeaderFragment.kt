/*
Copyright (C) 2014, 2020 Boris Timofeev

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
import org.apache.commons.io.FilenameUtils
import org.emunix.unipatcher.*
import org.emunix.unipatcher.Utils.startForegroundService
import org.emunix.unipatcher.databinding.SnesSmcHeaderFragmentBinding
import org.emunix.unipatcher.tools.SnesSmcHeader
import timber.log.Timber

class SnesSmcHeaderFragment : ActionFragment(), View.OnClickListener {

    private var romPath: String = ""     //
    private var outputPath: String = ""  // String representation of Uri, example "content://com.app.name/path_to_file

    private var _binding: SnesSmcHeaderFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = SnesSmcHeaderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        UniPatcher.appComponent.inject(this)
        activity?.setTitle(R.string.nav_snes_add_del_smc_header)
        binding.romCardView.setOnClickListener(this)
        binding.outputCardView.setOnClickListener(this)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            romPath = savedInstanceState.getString("romPath") ?: ""
            outputPath = savedInstanceState.getString("outputPath") ?: ""
            if (romPath.isNotEmpty()) {
                val uri = Uri.parse(romPath)
                binding.romNameTextView.text = DocumentFile.fromSingleUri(requireContext(), uri)?.name ?: "unknown"
                checkSmc(uri)
            }
            if (outputPath.isNotEmpty())
                binding.outputNameTextView.text = DocumentFile.fromSingleUri(requireContext(), Uri.parse(outputPath))?.name ?: "unknown"
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("romPath", romPath)
        savedInstanceState.putString("outputPath", outputPath)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.romCardView -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                }
                startActivityForResult(intent, Action.SELECT_ROM_FILE)
            }
            R.id.outputCardView -> {
                var title = "headerless_rom.smc"
                if (romPath.isNotBlank()) {
                    val romName = DocumentFile.fromSingleUri(requireContext(), Uri.parse(romPath))?.name
                    if (romName != null)
                        title = makeOutputTitle(romName)
                }
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_TITLE, title)
                }
                startActivityForResult(intent, Action.SELECT_OUTPUT_FILE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        Timber.d("onActivityResult($requestCode, $resultCode, $resultData)")
        if (resultCode == Activity.RESULT_OK && resultData != null && (requestCode == Action.SELECT_ROM_FILE || requestCode == Action.SELECT_OUTPUT_FILE)) {
            resultData.data?.let { uri ->
                Timber.d(uri.toString())
                val takeFlags = resultData.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
                when (requestCode) {
                    Action.SELECT_ROM_FILE -> {
                        romPath = uri.toString()
                        binding.romNameTextView.visibility = View.VISIBLE
                        binding.romNameTextView.text = DocumentFile.fromSingleUri(requireContext(), uri)?.name ?: "unknown"
                        checkSmc(uri)
                    }
                    Action.SELECT_OUTPUT_FILE -> {
                        outputPath = uri.toString()
                        binding.outputNameTextView.visibility = View.VISIBLE
                        binding.outputNameTextView.text = DocumentFile.fromSingleUri(requireContext(), uri)?.name ?: "unknown"
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    fun checkSmc(uri: Uri) {
        val uriFileSize = DocumentFile.fromSingleUri(requireContext(), uri)?.length()
        if (uriFileSize == null || uriFileSize == 0L) {
            binding.headerInfoTextView.setText(R.string.snes_smc_error_unable_to_get_file_size)
            return
        }
        val checker = SnesSmcHeader()
        if (checker.isRomHasSmcHeader(uriFileSize)) {
            binding.headerInfoTextView.setText(R.string.snes_smc_header_will_be_removed)
        } else {
            binding.headerInfoTextView.setText(R.string.snes_rom_has_no_smc_header)
        }
    }

    private fun makeOutputTitle(romName: String): String {
        val baseName = FilenameUtils.getBaseName(romName)
        val ext = FilenameUtils.getExtension(romName)
        return "$baseName [headerless].$ext"
    }

    override fun runAction(): Boolean {
        if (romPath.isEmpty()) {
            Toast.makeText(activity, getString(R.string.main_activity_toast_rom_not_selected), Toast.LENGTH_LONG).show()
            return false
        }
        if (outputPath.isEmpty()) {
            Toast.makeText(activity, getString(R.string.main_activity_toast_output_not_selected), Toast.LENGTH_LONG).show()
            return false
        }
        val rom = DocumentFile.fromSingleUri(requireContext(), Uri.parse(romPath))
        val intent = Intent(activity, WorkerService::class.java)
        intent.putExtra("action", Action.SNES_REMOVE_SMC_HEADER)
        intent.putExtra("romPath", romPath)
        intent.putExtra("outputPath", outputPath)
        intent.putExtra("romName", rom?.name ?: "")
        startForegroundService(requireActivity(), intent)
        Toast.makeText(activity, R.string.notify_snes_delete_smc_header_stared_check_noify, Toast.LENGTH_SHORT).show()
        return true
    }
}