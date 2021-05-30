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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.unipatcher.R
import org.emunix.unipatcher.databinding.CreatePatchFragmentBinding
import org.emunix.unipatcher.ktx.registerActivityResult
import org.emunix.unipatcher.viewmodels.ActionIsRunningViewModel
import org.emunix.unipatcher.viewmodels.CreatePatchViewModel

@AndroidEntryPoint
class CreatePatchFragment : ActionFragment(), View.OnClickListener {

    private val viewModel by viewModels<CreatePatchViewModel>()
    private val actionIsRunningViewModel by viewModels<ActionIsRunningViewModel>()

    private lateinit var activitySourceFile: ActivityResultLauncher<Intent>
    private lateinit var activityModifiedFile: ActivityResultLauncher<Intent>
    private lateinit var activityPatchFile: ActivityResultLauncher<Intent>

    private var _binding: CreatePatchFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CreatePatchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.setTitle(R.string.nav_create_patch)

        activitySourceFile = registerActivityResult(viewModel::sourceSelected)
        activityModifiedFile = registerActivityResult(viewModel::modifiedSelected)
        activityPatchFile = registerActivityResult(viewModel::patchSelected)

        viewModel.getSourceName().observe(viewLifecycleOwner, {
            binding.sourceFileNameTextView.text = it
        })
        viewModel.getModifiedName().observe(viewLifecycleOwner, {
            binding.modifiedFileNameTextView.text = it
        })
        viewModel.getPatchName().observe(viewLifecycleOwner, {
            binding.patchFileNameTextView.text = it
        })
        viewModel.getMessage().observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
        viewModel.getActionIsRunning().observe(viewLifecycleOwner, { isRunning ->
            actionIsRunningViewModel.createPatch(isRunning)
            binding.progressBar.isVisible = isRunning
        })
        binding.sourceFileCardView.setOnClickListener(this)
        binding.modifiedFileCardView.setOnClickListener(this)
        binding.patchFileCardView.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.sourceFileCardView -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                try {
                    activitySourceFile.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), R.string.error_file_picker_app_is_no_installed, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.modifiedFileCardView -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                try {
                    activityModifiedFile.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), R.string.error_file_picker_app_is_no_installed, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.patchFileCardView -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_TITLE, "patch.xdelta")
                }
                try {
                    activityPatchFile.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), R.string.error_file_picker_app_is_no_installed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun runAction() {
        viewModel.runActionClicked()
    }
}