/*
Copyright (C) 2013-2017 Boris Timofeev

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

public class Globals {
    private static String cmdArgument = null;

    public static String getCmdArgument() {
        return cmdArgument;
    }

    public static void setCmdArgument(String cmdArgument) {
        Globals.cmdArgument = cmdArgument;
    }

    public static final int ACTION_PATCHING = 1;
    public static final int ACTION_CREATE_PATCH = 2;
    public static final int ACTION_SMD_FIX_CHECKSUM = 3;
    public static final int ACTION_SNES_ADD_SMC_HEADER = 4;
    public static final int ACTION_SNES_DELETE_SMC_HEADER = 5;
}
