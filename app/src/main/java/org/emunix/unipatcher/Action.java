/*
 Copyright (c) 2017 Boris Timofeev

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

package org.emunix.unipatcher;

public class Action {
    public static final int SELECT_ROM_FILE = 1;
    public static final int SELECT_PATCH_FILE = 2;
    public static final int SELECT_SOURCE_FILE = 3;
    public static final int SELECT_MODIFIED_FILE = 4;
    public static final int SELECT_HEADER_FILE = 5;

    public static final int APPLY_PATCH = 101;
    public static final int CREATE_PATCH = 102;
    public static final int SMD_FIX_CHECKSUM = 103;
    public static final int SNES_ADD_SMC_HEADER = 104;
    public static final int SNES_DELETE_SMC_HEADER = 105;
}
