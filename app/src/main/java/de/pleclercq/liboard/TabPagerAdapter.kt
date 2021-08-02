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

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.pleclercq.liboard.chessclock.ChessClock
import de.pleclercq.liboard.chessclock.TimeControl
import de.pleclercq.liboard.databinding.ChessclockBinding
import de.pleclercq.liboard.liboard.Game
import de.pleclercq.liboard.liboard.LiBoard
import de.pleclercq.liboard.liboard.toPgn
import de.pleclercq.liboard.util.ClockHolder
import de.pleclercq.liboard.util.TextViewHolder
import de.pleclercq.liboard.util.ViewHolder

@ExperimentalUnsignedTypes
class TabPagerAdapter(private val liBoard: LiBoard) : RecyclerView.Adapter<ViewHolder>() {
	private var clock = ChessClock(TimeControl(180, 2))
	private var data = getData()
	private val handler = Handler(Looper.getMainLooper())
	private val runnable = Runnable { onTick() }

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return when (viewType) {
			TEXT_BIG -> TextViewHolder(parent.context)
			TEXT_SMALL -> TextViewHolder(parent.context, 12F, TextView.TEXT_ALIGNMENT_TEXT_START)
			CLOCK -> ClockHolder(
				ChessclockBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
			) { v -> onClick(v) }
			else -> throw NotImplementedError()
		}
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.updateContents(data[position].first)

	override fun getItemCount() = data.size

	override fun getItemViewType(position: Int) = data[position].second

	fun updateData() {
		val tmp = getData()
		for (i in (0..tmp.lastIndex)) if (tmp[i] != data[i]) notifyItemChanged(i)
		data = tmp
	}

	private fun getData() = arrayOf(
		Pair(liBoard.board.toString(), TEXT_BIG),
		Pair(Game(liBoard).toPgn(), TEXT_SMALL),
		Pair(liBoard.physicalPosition.toString(), TEXT_BIG),
		Pair(clock.toString(), CLOCK)
	)

	private fun onTick() {
		updateData()
		if (clock.running) handler.postDelayed(runnable, CLOCK_REFRESH_RATE)
	}

	private fun onClick(view: View) {
		when (view.id) {
			R.id.clock_black -> {
				clock.side = ChessClock.WHITE
				startClock()
			}
			R.id.clock_stop -> clock.running = false
			R.id.clock_white -> {
				clock.side = ChessClock.BLACK
				startClock()
			}
		}
	}

	private fun startClock() {
		clock.running = true
		handler.post(runnable)
	}

	companion object {
		const val TEXT_BIG = 0
		const val TEXT_SMALL = 1
		const val CLOCK = 2
		const val CLOCK_REFRESH_RATE = 250L
	}
}