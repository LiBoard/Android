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

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import de.pleclercq.liboard.liboard.findMove
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MoveGenTest {
	@Test
	fun moveGenLegalNormal() {
		val b = Board()
		assertNotNull(b.findMove(Square.E2, Square.E4))
	}

	@Test
	fun moveGenIllegalNormal() {
		val b = Board()
		assertNull(b.findMove(Square.A1, Square.E4))
	}
}