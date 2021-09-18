/*
 Copyright (c) 2020-2021 Boris Timofeev

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

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.emunix.unipatcher.Settings
import org.emunix.unipatcher.SettingsImpl
import org.emunix.unipatcher.utils.UFileUtils
import org.emunix.unipatcher.helpers.ResourceProvider
import org.emunix.unipatcher.helpers.ResourceProviderImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ProviderModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Reusable
    fun provideSettings(prefs: SharedPreferences): Settings = SettingsImpl(prefs)

    @Provides
    @Singleton
    fun provideResourceProvider(
        @ApplicationContext context: Context
    ): ResourceProvider = ResourceProviderImpl(context)

    @Provides
    @Singleton
    fun provideFileUtils(
        @ApplicationContext context: Context,
        resourceProvider: ResourceProvider
    ): UFileUtils = UFileUtils(context, resourceProvider)
}