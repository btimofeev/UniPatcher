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

package org.emunix.unipatcher.helpers

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import android.provider.OpenableColumns
import javax.inject.Inject

class UriParserImpl @Inject constructor(val context: Context) : UriParser {

    override fun getFileName(uri: Uri): String? {
        require(uri.scheme == ContentResolver.SCHEME_CONTENT) { "uri is not contain content:// scheme" }

        return getFileNameSchemeContent(uri)
    }

    private fun getFileNameSchemeContent(uri: Uri): String? {
        var result: String? = null
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor.use { cursor ->
            if (cursor != null && cursor.moveToFirst())
                result = cursor.getString(0)
        }
        return result
    }

    override fun getFileSize(uri: Uri): Long {
        require(uri.scheme == ContentResolver.SCHEME_CONTENT) { "uri is not contain content:// scheme" }

        return getFileSizeSchemeContent(uri)
    }

    private fun getFileSizeSchemeContent(uri: Uri): Long {
        var result = 0L
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        cursor.use { cursor ->
            if (cursor != null && cursor.moveToFirst())
                result = cursor.getLong(0)
        }
        return result
    }

    override fun isExist(uri: Uri): Boolean {
        require(uri.scheme == ContentResolver.SCHEME_CONTENT) { "uri is not contain content:// scheme" }
        val cursor = context.contentResolver.query(uri, arrayOf(BaseColumns._ID), null, null, null, null)
        cursor.use {
            return it != null && it.moveToFirst()
        }
    }
}