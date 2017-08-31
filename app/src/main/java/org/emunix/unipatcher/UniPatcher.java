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

import android.app.Application;

public class UniPatcher extends Application {
    private static String appArgument = null;

    public static String getAppArgument() {
        return appArgument;
    }

    public static void setAppArgument(String appArgument) {
        UniPatcher.appArgument = appArgument;
    }
}
