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

data class TimeControl(@Size(2) val initialTimes: IntArray, @Size(2) val increments: IntArray) {
	val hasTimeOdds get() = initialTimes.toSet().size > 1 || increments.toSet().size > 1

	constructor(initialTime: Int, increment: Int) : this(
		intArrayOf(initialTime, initialTime),
		intArrayOf(increment, increment)
	)

	init {
		if (initialTimes.size != 2 || increments.size != 2) throw IndexOutOfBoundsException()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as TimeControl

		if (!initialTimes.contentEquals(other.initialTimes)) return false
		if (!increments.contentEquals(other.increments)) return false

		return true
	}

	override fun hashCode(): Int {
		var result = initialTimes.contentHashCode()
		result = 31 * result + increments.contentHashCode()
		return result
	}
}
