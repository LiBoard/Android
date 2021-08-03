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

import android.graphics.Color
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import de.pleclercq.liboard.R.color.Tomato
import de.pleclercq.liboard.R.color.colorPrimaryLight
import de.pleclercq.liboard.chessclock.ChessClock
import de.pleclercq.liboard.chessclock.ChessClock.Companion.BLACK
import de.pleclercq.liboard.chessclock.ChessClock.Companion.WHITE
import de.pleclercq.liboard.databinding.ChessclockBinding
import java.io.InvalidClassException

class ClockHolder(private val binding: ChessclockBinding, onClick: (View) -> Unit) : ViewHolder(binding.root) {
	private val clockViews = arrayOf(binding.clockWhite, binding.clockBlack)

	init {
		clockViews.forEach { it.setOnClickListener(onClick) }
		binding.clockStop.setOnClickListener(onClick)
	}

	override fun updateContents(data: Any) {
		if (data !is ChessClock) throw InvalidClassException("Expected ChessClock, got ${data::class.simpleName}")
		binding.clockWhite.text = formatTime(data.getCurrentTime(WHITE))
		binding.clockBlack.text = formatTime(data.getCurrentTime(BLACK))
		for (i in clockViews.indices) updateClockView(data, i)
	}

	private fun formatTime(time: Int): String {
		val tenths = (time / 100) % 10
		val seconds = (time / 1000) % 60
		val minutes = time / 60000

		return "%02d:%02d".format(minutes, seconds) + if (minutes < 1) "%01d".format(tenths) else ""
	}

	private fun updateClockView(clock: ChessClock, side: Int) {
		val view = clockViews[side]
		val time = clock.getCurrentTime(side)
		val color =
			when {
				time < 0 -> getColor(Tomato)
				clock.running && clock.side == side -> getColor(colorPrimaryLight)
				else -> Color.TRANSPARENT
			}
		view.background.setTint(color)
	}

	private fun getColor(@ColorRes colorRes: Int) = ContextCompat.getColor(binding.root.context, colorRes)
}