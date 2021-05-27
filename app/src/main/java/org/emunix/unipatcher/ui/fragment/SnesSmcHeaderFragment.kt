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
import org.emunix.unipatcher.R
import org.emunix.unipatcher.databinding.SnesSmcHeaderFragmentBinding
import org.emunix.unipatcher.ktx.registerActivityResult
import org.emunix.unipatcher.viewmodels.ActionIsRunningViewModel
import org.emunix.unipatcher.viewmodels.SnesSmcHeaderViewModel

class SnesSmcHeaderFragment : ActionFragment(), View.OnClickListener {

    private val viewModel by viewModels<SnesSmcHeaderViewModel>()
    private val actionIsRunningViewModel by viewModels<ActionIsRunningViewModel>()

    private lateinit var activityRomFile: ActivityResultLauncher<Intent>
    private lateinit var activityOutputFile: ActivityResultLauncher<Intent>

    private var _binding: SnesSmcHeaderFragmentBinding? = null
    private val binding get() = _binding!!

    private var suggestedOutputName: String = "headerless_rom.smc"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SnesSmcHeaderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.setTitle(R.string.nav_snes_add_del_smc_header)

        activityRomFile = registerActivityResult(viewModel::romSelected)
        activityOutputFile = registerActivityResult(viewModel::outputSelected)

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
        viewModel.getInfoText().observe(viewLifecycleOwner, { text ->
            binding.headerInfoTextView.text = text
        })
        viewModel.getActionIsRunning().observe(viewLifecycleOwner, { isRunning ->
            actionIsRunningViewModel.removeSmc(isRunning)
            binding.progressBar.isVisible = isRunning
        })

        binding.romCardView.setOnClickListener(this)
        binding.outputCardView.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.romCardView -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                try {
                    activityRomFile.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), R.string.error_file_picker_app_is_no_installed, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.outputCardView -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_TITLE, suggestedOutputName)
                }
                try {
                    activityOutputFile.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), R.string.error_file_picker_app_is_no_installed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun runAction(){
        viewModel.runActionClicked()
    }
}