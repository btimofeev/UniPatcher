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
import org.emunix.unipatcher.Action
import org.emunix.unipatcher.R
import org.emunix.unipatcher.UniPatcher
import org.emunix.unipatcher.Utils.startForegroundService
import org.emunix.unipatcher.WorkerService
import org.emunix.unipatcher.databinding.SmdFixChecksumFragmentBinding
import org.emunix.unipatcher.helpers.UriParser
import timber.log.Timber
import javax.inject.Inject

class SmdFixChecksumFragment : ActionFragment(), View.OnClickListener {

    private var romPath: String = ""  // String representation of Uri, example "content://com.app.name/path_to_file"

    private var _binding: SmdFixChecksumFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var uriParser: UriParser

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
        UniPatcher.appComponent.inject(this)
        activity?.setTitle(R.string.nav_smd_fix_checksum)
        binding.romCardView.setOnClickListener(this)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            romPath = savedInstanceState.getString("romPath") ?: ""
            if (romPath.isNotEmpty())
                binding.romNameTextView.text = uriParser.getFileName(Uri.parse(romPath))
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("romPath", romPath)
    }

    override fun onClick(view: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
        }

        startActivityForResult(intent, Action.SELECT_ROM_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        Timber.d("onActivityResult($requestCode, $resultCode, $resultData)")
        if (requestCode == Action.SELECT_ROM_FILE && resultCode == Activity.RESULT_OK && resultData != null) {
            resultData.data?.let { uri ->
                Timber.d(uri.toString())
                val takeFlags = resultData.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
                uriParser.getFileName(uri)?.let { fileName ->
                    romPath = uri.toString()
                    binding.romNameTextView.visibility = View.VISIBLE
                    binding.romNameTextView.text = fileName
                }
            }
        }
    }

    override fun runAction(): Boolean {
        if (romPath.isEmpty()) {
            Toast.makeText(activity, getString(R.string.main_activity_toast_rom_not_selected), Toast.LENGTH_LONG).show()
            return false
        }
        val intent = Intent(activity, WorkerService::class.java)
        intent.putExtra("romPath", romPath)
        intent.putExtra("action", Action.SMD_FIX_CHECKSUM)
        startForegroundService(requireActivity(), intent)
        Toast.makeText(activity, R.string.notify_smd_fix_checksum_started_check_notify, Toast.LENGTH_SHORT).show()
        return true
    }
}