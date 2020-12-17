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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.emunix.unipatcher.Action
import org.emunix.unipatcher.R
import org.emunix.unipatcher.databinding.SmdFixChecksumFragmentBinding
import org.emunix.unipatcher.viewmodels.ActionIsRunningViewModel
import org.emunix.unipatcher.viewmodels.SmdFixChecksumViewModel
import timber.log.Timber

class SmdFixChecksumFragment : ActionFragment(), View.OnClickListener {
    private lateinit var viewModel: SmdFixChecksumViewModel
    private lateinit var actionIsRunningViewModel: ActionIsRunningViewModel

    private var _binding: SmdFixChecksumFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = SmdFixChecksumFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.setTitle(R.string.nav_smd_fix_checksum)

        actionIsRunningViewModel = ViewModelProvider(requireActivity()).get(ActionIsRunningViewModel::class.java)
        viewModel = ViewModelProvider(requireActivity()).get(SmdFixChecksumViewModel::class.java)
        viewModel.getRomName().observe(viewLifecycleOwner, Observer {
            binding.romNameTextView.text = it
        })
        viewModel.getMessage().observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
        viewModel.getActionIsRunning().observe(viewLifecycleOwner, Observer { isRunning ->
            actionIsRunningViewModel.fixChecksum(isRunning)
            if(isRunning) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.INVISIBLE
            }
        })

        binding.romCardView.setOnClickListener(this)
    }

    override fun onClick(view: View) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        Timber.d("onActivityResult($requestCode, $resultCode, $resultData)")
        if (requestCode == Action.SELECT_ROM_FILE && resultCode == Activity.RESULT_OK && resultData != null) {
            resultData.data?.let { uri ->
                Timber.d(uri.toString())
                viewModel.romSelected(uri)
            }
        }
    }

    override fun runAction() {
        viewModel.runActionClicked()
    }
}