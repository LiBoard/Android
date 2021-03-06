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

import androidx.annotation.Size

open class ChessClock(protected val timeControl: TimeControl) {
	@Size(2)
	protected var times = initialTimes
	var side = WHITE
		set(value) {
			if (value != field) {
				if (running) {
					storeTimes()
					applyIncrement(field)
					timeSideStarted = sysTime
				}
				field = value
			}
		}
	var running = false
		set(value) {
			if (value != field) {
				if (value) {
					if (flagged == null) timeSideStarted = sysTime
				} else
					storeTimes()
				field = value
			}
		}
	private var timeSideStarted = sysTime
	private val sysTime get() = System.currentTimeMillis()
	private val initialTimes get() = IntArray(2) { i -> timeControl.initialTimes[i] * RESOLUTION }
	val flagged: Int?
		get() {
			val r = intArrayOf(
				WHITE,
				BLACK
			).firstOrNull { getCurrentTime(it) < 0 } // the '<' is essential for Stopwatch mode
			if (r != null)
				running = false
			return r
		}
	protected val timeDelta get() = (sysTime - timeSideStarted).toInt()

	fun reset() {
		running = false
		side = WHITE
		times = initialTimes
	}

	protected open fun applyIncrement(_side: Int) {
		times[_side] += timeControl.increments[_side] * RESOLUTION
	}

	open fun getCurrentTime(_side: Int) = times[_side] - (if (running && _side == side) timeDelta else 0)
	private fun storeTimes() {
		times = intArrayOf(getCurrentTime(WHITE), getCurrentTime(BLACK))
	}

	companion object {
		const val WHITE = 0
		const val BLACK = 1
		const val RESOLUTION = 1000 // Hz -> 1000Hz = millisecond accuracy
	}
}