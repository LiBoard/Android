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

package de.pleclercq.liboard.android.viewHolders

import android.graphics.Color
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.children
import de.pleclercq.liboard.R.color.Goldenrod
import de.pleclercq.liboard.R.color.Tomato
import de.pleclercq.liboard.chessclock.ClockSnapshot
import de.pleclercq.liboard.databinding.ChessclockBinding
import java.io.InvalidClassException
import kotlin.math.max

class ClockHolder(private val binding: ChessclockBinding, onClick: (View) -> Unit) : ViewHolder(binding.root) {
	private val clockViews = arrayOf(binding.clockWhite, binding.clockBlack)

	init {
		for (child in binding.root.children) child.setOnClickListener(onClick)
	}

	override fun updateContents(data: Any) {
		if (data !is ClockSnapshot) throw InvalidClassException("Expected ClockSnapshot, got ${data::class.simpleName}")
		for (i in clockViews.indices) updateClockView(data, i)
	}

	private fun formatTime(time: Int): String {
		val tenths = (time / 100) % 10
		val seconds = (time / 1000) % 60
		val minutes = time / 60000

		return "%02d:%02d".format(minutes, seconds) + if (minutes < 1) ".%01d".format(tenths) else ""
	}

	private fun updateClockView(snapshot: ClockSnapshot, side: Int) {
		binding.clockStop.isEnabled = snapshot.running
		clockViews[side].apply {
			text = formatTime(max(0, snapshot.getCurrentTime(side)))
			background.setTint(
				when {
					snapshot.flagged == side -> getColor(Tomato)
					snapshot.running && snapshot.side == side -> getColor(Goldenrod)
					else -> Color.TRANSPARENT
				}
			)
		}
	}

	private fun getColor(@ColorRes colorRes: Int) = ContextCompat.getColor(binding.root.context, colorRes)
}