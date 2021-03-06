/*
 * LiBoard
 * Copyright (C) 2021 Philipp Leclercq
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package de.pleclercq.liboard

import de.pleclercq.liboard.liboard.PhysicalPosition
import org.junit.Test

@ExperimentalUnsignedTypes
class PhysicalPositionTest {
	@Test
	fun startingPosOccupiedSquares() {
		val o = PhysicalPosition.STARTING_POSITION.occupiedSquares
		for (i in (0 until 16) + (48 until 64))
			assert(o.contains(i))
		for (i in 16 until 48)
			assert(!o.contains(i))
	}
}