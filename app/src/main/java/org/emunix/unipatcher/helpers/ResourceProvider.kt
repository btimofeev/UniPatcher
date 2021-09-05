/*
 Copyright (c) 2021 Boris Timofeev

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

package org.emunix.unipatcher.helpers

import android.content.Context
import androidx.annotation.StringRes
import java.io.File
import java.io.InputStream
import javax.inject.Inject

/**
 * Application resource provider
 */
interface ResourceProvider {

    /**
     * Returns the absolute path to the application specific cache directory on the filesystem.
     */
    val cacheDir: File

    /**
     * Open file from "assets" directory
     * @param fileName file name
     * @return file content as [InputStream]
     */
    fun getAsset(fileName: String): InputStream

    /**
     * Returns a localized string from the application's package's default string table.
     * @param resId – Resource id for the string
     * @return The string data associated with the resource, stripped of styled text information.
     */
    fun getString(@StringRes resId: Int): String
}

/**
 * Implementation of [ResourceProvider]
 *
 * @param context application context
 */
class ResourceProviderImpl @Inject constructor(private val context: Context) : ResourceProvider {

    override val cacheDir: File
        get() = context.cacheDir

    override fun getAsset(fileName: String): InputStream = context.assets.open(fileName)

    override fun getString(resId: Int): String = context.getString(resId)
}