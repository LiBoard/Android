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

package de.pleclercq.liboard.util

import android.view.View
import de.pleclercq.liboard.chessclock.ChessClock
import de.pleclercq.liboard.databinding.ChessclockBinding
import java.io.InvalidClassException

class ClockHolder(private val binding: ChessclockBinding, onClick: (View) -> Unit) : ViewHolder(binding.root) {
	init {
		binding.clockBlack.setOnClickListener(onClick)
		binding.clockWhite.setOnClickListener(onClick)
		binding.clockStop.setOnClickListener(onClick)
	}

	override fun updateContents(data: Any) {
		if (data !is Triple<*, *, *> || data.first !is Int || data.second !is Int || data.third !is Int?)
			throw InvalidClassException("Expected Triple<Int, Int, Int?>")
		// TODO Better formatting
		binding.clockWhite.text = "${data.first as Int / ChessClock.RESOLUTION}"
		binding.clockBlack.text = "${data.second as Int / ChessClock.RESOLUTION}"
	}
}