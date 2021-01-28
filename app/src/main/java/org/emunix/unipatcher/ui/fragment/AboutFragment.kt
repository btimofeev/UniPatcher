/*
Copyright (C) 2016, 2021 Boris Timofeev

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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import org.emunix.unipatcher.R
import org.emunix.unipatcher.Utils
import org.emunix.unipatcher.databinding.FragmentAboutBinding
import org.emunix.unipatcher.helpers.SocialHelper
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment : Fragment() {

    @Inject
    lateinit var social: Lazy<SocialHelper>

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.versionText.text = getString(R.string.help_activity_about_tab_version, Utils.getAppVersion(requireActivity()))
        binding.sendFeedbackButton.setOnClickListener { social.get().sendFeedback() }
        binding.visitSiteButton.setOnClickListener { social.get().openWebsite() }
        binding.changelogButton.setOnClickListener { social.get().showChangelog() }
    }
}