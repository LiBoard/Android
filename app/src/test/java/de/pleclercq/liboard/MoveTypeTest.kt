/*
 * LiBoard
 * Copyright (C) 2021 Philipp Leclercq
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.pleclercq.liboard

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square.*
import com.github.bhlangonijr.chesslib.move.Move
import de.pleclercq.liboard.liboard.*
import org.junit.Assert.assertNotNull
import org.junit.Test

class MoveTypeTest {
	@Test
	fun normalMove() {
		val b = Board()
		val m = b.findMove(E2, E4)!!
		assert(!b.isCastling(m))
		assert(!b.isNormalCapture(m))
		assert(!b.isEnPassant(m))
		assert(!b.isCapture(m))
	}

	@Test
	fun capture() {
		val b = Board()
		for (m in listOf(Move(E2, E4), Move(C7, C6), Move(D2, D4), Move(D7, D5))) assert(b.doMove(m, true))
		val m = b.findMove(E4, D5)!!
		assertNotNull(m)
		assert(b.isNormalCapture(m))
		assert(!b.isEnPassant(m))
		assert(b.isCapture(m))
		assert(!b.isCastling(m))
	}

	@Test
	fun enPassant() {
		val b = Board()
		for (m in listOf(Move(E2, E4), Move(C7, C6), Move(E4, E5), Move(D7, D5))) assert(b.doMove(m, true))
		val m = b.findMove(E5, D6)!!
		assertNotNull(m)
		assert(!b.isNormalCapture(m))
		assert(b.isEnPassant(m))
		assert(b.isCapture(m))
		assert(!b.isCastling(m))
	}

	@Test
	fun castling() {
		val b = Board()
		b.loadFromFen("r3k2r/pp2qppp/b1np4/4p3/8/1QN4P/PPP2PP1/R3K2R w KQkq - 4 16")

		val queensideCastle = b.findMove(E1, C1)!!
		assertNotNull(queensideCastle)
		assert(!b.isNormalCapture(queensideCastle))
		assert(!b.isCapture(queensideCastle))
		assert(!b.isEnPassant(queensideCastle))
		assert(b.isCastling(queensideCastle))
		assert(b.doMove(queensideCastle, true))

		val kingsideCastle = b.findMove(E8, G8)!!
		assertNotNull(kingsideCastle)
		assert(!b.isNormalCapture(kingsideCastle))
		assert(!b.isCapture(kingsideCastle))
		assert(!b.isEnPassant(kingsideCastle))
		assert(b.isCastling(kingsideCastle))
		assert(b.doMove(kingsideCastle, true))
	}
}