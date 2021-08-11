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

package de.pleclercq.liboard.chessclock

data class ClockSnapshot(val tWhite: Int, val tBlack: Int, val flagged: Int?, val running: Boolean, val side: Int) {
	constructor(clock: ChessClock) : this(
		clock.getCurrentTime(ChessClock.WHITE),
		clock.getCurrentTime(ChessClock.BLACK),
		clock.flagged,
		clock.running,
		clock.side
	)

	fun getCurrentTime(side: Int) = if (side == ChessClock.WHITE) tWhite else tBlack
}