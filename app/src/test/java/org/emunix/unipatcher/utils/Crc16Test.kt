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

package org.emunix.unipatcher.utils

import org.junit.Assert.*

import org.junit.*

class Crc16Test {

    private val crc16 = Crc16()

    @Test
    fun `check crc16 with A`() {
        assertEquals(0xB915, crc16.calculate("A".toByteArray()))
    }

    @Test
    fun `check crc16 with 123456789`() {
        assertEquals(0x29B1, crc16.calculate("123456789".toByteArray()))
    }

    @Test
    fun `check crc16 with empty value`() {
        assertEquals(0xFFFF, crc16.calculate("".toByteArray()))
    }
}