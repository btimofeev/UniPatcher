/*
Copyright (C) 2013, 2021 Boris Timofeev

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

package org.emunix.unipatcher.patcher;

import java.io.File;
import java.io.IOException;
import org.emunix.unipatcher.helpers.ResourceProvider;

public abstract class Patcher {

    protected File patchFile;
    protected File romFile;
    protected File outputFile;
    protected ResourceProvider resourceProvider;

    public Patcher(File patch, File rom, File output, ResourceProvider resourceProvider) {
        patchFile = patch;
        romFile = rom;
        outputFile = output;
        this.resourceProvider = resourceProvider;
    }

    public abstract void apply(boolean ignoreChecksum) throws PatchException, IOException;
}
