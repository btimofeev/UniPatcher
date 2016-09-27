/*
Copyright (C) 2013-2016 Boris Timofeev

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
    private static final String KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA384jTCBEuJ8nCWaC4S6AFrnMQN4mBlmkOXHV3Xg5hlFOl8TkVwiCfqz8r20yJpEy0IJ1+3QRnlq59zadUxbkD+PacJlGB/r2b3mbKfu+m0K+e/0aL6eWupjMSIyPgpnbN3uswiBEGUb4ytzYF53ZKTbLARnruQdMnjV6+VyfwMgpor/48anVQawDARBj/AIAj6VGtRHLmg6DmKDyOGQ7uCgXSv+ysnBKJjtIX/L/5nQgL8Q+9jsr2knuWY7j9BmrtpUXaDH3Kb50M1TOCKiqxPGa8lInOOIndABWxcpqmSMXP06SPYOanUlEH7lT0jjqpHpFNx8hRTT9xf652rgMJwIDAQAB";
    private static String cmdArgument = null;
    private static boolean isFullVersion = false;

    public static String getCmdArgument() {
        return cmdArgument;
    }

    public static void setCmdArgument(String cmdArgument) {
        Globals.cmdArgument = cmdArgument;
    }

    public static boolean isFullVersion() {
        return isFullVersion;
    }

    public static void setFullVersion() {
        isFullVersion = true;
    }

    public static String getKey() {
        return KEY;
    }

    public static final int ACTION_PATCHING = 1;
    public static final int ACTION_SMD_FIX_CHECKSUM = 2;
    public static final int ACTION_SNES_ADD_SMC_HEADER = 3;
    public static final int ACTION_SNES_DELETE_SMC_HEADER = 4;
}
