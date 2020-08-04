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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import org.emunix.unipatcher.Action
import org.emunix.unipatcher.R
import org.emunix.unipatcher.Settings
import org.emunix.unipatcher.Utils.isArchive
import org.emunix.unipatcher.Utils.startForegroundService
import org.emunix.unipatcher.WorkerService
import org.emunix.unipatcher.databinding.SnesSmcHeaderFragmentBinding
import org.emunix.unipatcher.tools.SnesSmcHeader
import org.emunix.unipatcher.ui.activity.FilePickerActivity
import timber.log.Timber
import java.io.File

class SnesSmcHeaderFragment : ActionFragment(), View.OnClickListener {

    private var romPath: String = ""
    private var headerPath: String = ""
    private var action = 0

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
        activity?.setTitle(R.string.nav_snes_add_del_smc_header)
        binding.romCardView.setOnClickListener(this)
        binding.headerCardView.setOnClickListener(this)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            romPath = savedInstanceState.getString("romPath") ?: ""
            headerPath = savedInstanceState.getString("headerPath") ?: ""
            action = savedInstanceState.getInt("action")
            when (action) {
                Action.SNES_ADD_SMC_HEADER -> {
                    binding.headerInfoTextView.setText(R.string.snes_smc_header_will_be_added)
                    binding.headerCardView.visibility = View.VISIBLE
                }
                Action.SNES_DELETE_SMC_HEADER -> {
                    binding.headerInfoTextView.setText(R.string.snes_smc_header_will_be_removed)
                    binding.headerCardView.visibility = View.GONE
                }
            }
            if (romPath.isNotEmpty()) binding.romNameTextView.text = File(romPath).name
            if (headerPath.isNotEmpty()) binding.headerNameTextView.text = File(headerPath).name
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("romPath", romPath)
        savedInstanceState.putString("headerPath", headerPath)
        savedInstanceState.putInt("action", action)
    }

    override fun onClick(view: View) {
        val intent = Intent(activity, FilePickerActivity::class.java)
        when (view.id) {
            R.id.romCardView -> {
                intent.putExtra("title", getString(R.string.file_picker_activity_title_select_rom))
                intent.putExtra("directory", Settings.getRomDir())
                startActivityForResult(intent, Action.SELECT_ROM_FILE)
            }
            R.id.headerCardView -> {
                intent.putExtra("title", getString(R.string.file_picker_activity_title_select_header))
                startActivityForResult(intent, Action.SELECT_HEADER_FILE)
            }
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
            if (isArchive(path)) {
                Toast.makeText(activity, R.string.main_activity_toast_archives_not_supported, Toast.LENGTH_LONG).show()
            }
            when (requestCode) {
                Action.SELECT_ROM_FILE -> {
                    romPath = path
                    binding.romNameTextView.visibility = View.VISIBLE
                    binding.romNameTextView.text = File(path).name
                    val dir = File(path).parent
                    if (dir != null) {
                        Settings.setLastRomDir(dir)
                    }
                    val checker = SnesSmcHeader()
                    if (checker.isHasSmcHeader(File(path))) {
                        action = Action.SNES_DELETE_SMC_HEADER
                        binding.headerCardView.visibility = View.GONE
                        binding.headerInfoTextView.setText(R.string.snes_smc_header_will_be_removed)
                    } else {
                        action = Action.SNES_ADD_SMC_HEADER
                        binding.headerCardView.visibility = View.VISIBLE
                        binding.headerInfoTextView.setText(R.string.snes_smc_header_will_be_added)
                    }
                    headerPath = ""
                    binding.headerNameTextView.setText(R.string.main_activity_tap_to_select)
                }
                Action.SELECT_HEADER_FILE -> {
                    headerPath = path
                    binding.headerNameTextView.text = File(path).name
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun runAction(): Boolean {
        if (romPath.isEmpty()) {
            Toast.makeText(activity, getString(R.string.main_activity_toast_rom_not_selected), Toast.LENGTH_LONG).show()
            return false
        }
        val intent = Intent(activity, WorkerService::class.java)
        intent.putExtra("action", action)
        intent.putExtra("romPath", romPath)
        intent.putExtra("headerPath", headerPath)
        startForegroundService(requireActivity(), intent)
        if (action == Action.SNES_ADD_SMC_HEADER) {
            Toast.makeText(activity, R.string.notify_snes_add_smc_header_stared_check_noify, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, R.string.notify_snes_delete_smc_header_stared_check_noify, Toast.LENGTH_SHORT).show()
        }
        return true
    }
}