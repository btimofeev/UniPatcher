/*
 Copyright (c) 2020 Boris Timofeev

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

package org.emunix.unipatcher.di

import android.content.SharedPreferences
import dagger.Component
import org.emunix.unipatcher.WorkerService
import org.emunix.unipatcher.ui.activity.MainActivity
import org.emunix.unipatcher.ui.fragment.CreatePatchFragment
import org.emunix.unipatcher.ui.fragment.SmdFixChecksumFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, PreferenceModule::class, UriParserModule::class])
interface AppComponent {

    fun inject(activity: MainActivity)
    fun inject(fragment: SmdFixChecksumFragment)
    fun inject(fragment: CreatePatchFragment)
    fun inject(service: WorkerService)

    fun sharedPreferences(): SharedPreferences
}