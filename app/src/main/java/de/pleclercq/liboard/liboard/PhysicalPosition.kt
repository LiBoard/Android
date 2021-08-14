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

package de.pleclercq.liboard.liboard

import com.github.bhlangonijr.chesslib.Board

/**
 * Represents a physical board position.
 *
 * @property bitboard The raw board data as a [ULong].
 * @property occupiedSquares A [Set] of the indices of the occupied squares.
 */
@ExperimentalUnsignedTypes
internal class PhysicalPosition(val bitboard: ULong) {
	val occupiedSquares = ((0..63).filter { bitboard and (1UL shl it) != 0UL }).toSet()

	constructor(board: Board) : this(board.bitboard.toULong())

	override fun equals(other: Any?): Boolean {
		return if (other is PhysicalPosition) {
			bitboard == other.bitboard
		} else super.equals(other)
	}

	override fun hashCode(): Int {
		return bitboard.hashCode()
	}

	override fun toString(): String {
		var s = ""
		for (rank in 7 downTo 0) {
			for (file in 0..7)
				s += if (occupiedSquares.contains(rank * 8 + file)) "X" else "."
			s += "\n"
		}
		return s
	}

	companion object {
		val STARTING_POSITION = PhysicalPosition(0xFFFF00000000FFFFUL)
	}
}