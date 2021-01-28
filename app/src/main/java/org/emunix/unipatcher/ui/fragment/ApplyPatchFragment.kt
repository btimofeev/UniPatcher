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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import org.emunix.unipatcher.Action
import org.emunix.unipatcher.R
import org.emunix.unipatcher.Settings
import org.emunix.unipatcher.databinding.ApplyPatchFragmentBinding
import org.emunix.unipatcher.ui.activity.HelpActivity
import org.emunix.unipatcher.viewmodels.ActionIsRunningViewModel
import org.emunix.unipatcher.viewmodels.ApplyPatchViewModel
import timber.log.Timber

class ApplyPatchFragment : ActionFragment(), View.OnClickListener {

    private val viewModel by viewModels<ApplyPatchViewModel>()
    private val actionIsRunningViewModel by viewModels<ActionIsRunningViewModel>()

    private var _binding: ApplyPatchFragmentBinding? = null
    private val binding get() = _binding!!

    private var suggestedOutputName: String = "specify_rom_name"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ApplyPatchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.setTitle(R.string.nav_apply_patch)

        viewModel.getPatchName().observe(viewLifecycleOwner, {
            binding.patchNameTextView.text = it
        })
        viewModel.getRomName().observe(viewLifecycleOwner, {
            binding.romNameTextView.text = it
        })
        viewModel.getOutputName().observe(viewLifecycleOwner, {
            binding.outputNameTextView.text = it
        })
        viewModel.getSuggestedOutputName().observe(viewLifecycleOwner, {
            suggestedOutputName = it
        })
        viewModel.getMessage().observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
        viewModel.getActionIsRunning().observe(viewLifecycleOwner, { isRunning ->
            actionIsRunningViewModel.applyPatch(isRunning)
            binding.progressBar.isVisible = isRunning
        })

        binding.patchCardView.setOnClickListener(this)
        binding.romCardView.setOnClickListener(this)
        binding.outputCardView.setOnClickListener(this)
        binding.howToUseAppButton.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        binding.howToUseAppButton.isVisible = Settings.getShowHelpButton()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.patchCardView -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
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
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
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
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_TITLE, suggestedOutputName)
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
                Timber.d("$uri")
                when (requestCode) {
                    Action.SELECT_PATCH_FILE -> {
                        viewModel.patchSelected(uri)
                    }
                    Action.SELECT_ROM_FILE -> {
                        viewModel.romSelected(uri)
                    }
                    Action.SELECT_OUTPUT_FILE -> {
                        viewModel.outputSelected(uri)
                    }
                    else -> IllegalStateException("RequestCode is not valid: $requestCode")
                }
            }
            super.onActivityResult(requestCode, resultCode, resultData)
        }
    }

    override fun runAction() {
        viewModel.runActionClicked()
    }
}