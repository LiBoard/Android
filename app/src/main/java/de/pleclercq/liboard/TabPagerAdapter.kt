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
import de.pleclercq.liboard.chessclock.ChessClock.Companion.BLACK
import de.pleclercq.liboard.chessclock.ChessClock.Companion.WHITE
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
	private var items = fetchItems()
	private val handler = Handler(Looper.getMainLooper())
	private val runnable = Runnable { onTick() }

	init {
		setHasStableIds(true)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.updateContents(items[position].data)
	override fun getItemCount() = items.size
	override fun getItemViewType(position: Int) = items[position].type
	override fun getItemId(position: Int) = items[position].id

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		return when (viewType) {
			TYPE_TEXT_BIG -> TextViewHolder(parent.context)
			TYPE_TEXT_SMALL -> TextViewHolder(parent.context, 12F, TextView.TEXT_ALIGNMENT_TEXT_START)
			TYPE_CLOCK -> ClockHolder(
				ChessclockBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
			) { v -> onClick(v) }
			else -> throw NotImplementedError()
		}
	}

	fun updateItems() {
		val tmp = fetchItems()
		tmp.forEachIndexed { index, item ->
			// the payload makes the update way more efficient for some reason
			if (item != items[index]) notifyItemChanged(index, true)
		}
		items = tmp
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
		super.onBindViewHolder(holder, position, payloads)
	}

	private fun fetchItems() = arrayOf(
		Item(ID_BOARD, liBoard.board.toString()),
		Item(ID_MOVES, Game(liBoard).toPgn()),
		Item(ID_DIAGNOSTICS, liBoard.physicalPosition.toString()),
		Item(ID_CLOCK, Pair(clock.getCurrentTime(WHITE), clock.getCurrentTime(BLACK)))
	)

	private fun onTick() {
		updateItems()
		if (clock.running) handler.postDelayed(runnable, CLOCK_REFRESH_RATE)
	}

	private fun onClick(view: View) {
		when (view.id) {
			R.id.clock_black -> {
				clock.side = WHITE
				startClock()
			}
			R.id.clock_stop -> clock.running = false
			R.id.clock_white -> {
				clock.side = BLACK
				startClock()
			}
		}
	}

	private fun startClock() {
		clock.running = true
		handler.post(runnable)
	}

	companion object {
		const val TYPE_TEXT_BIG = 0
		const val TYPE_TEXT_SMALL = 1
		const val TYPE_CLOCK = 2
		const val CLOCK_REFRESH_RATE = 10L
		const val ID_BOARD = 0L
		const val ID_MOVES = 1L
		const val ID_DIAGNOSTICS = 2L
		const val ID_CLOCK = 3L
	}

	private data class Item(val id: Long, val data: Any) {
		val type
			get() = when (id) {
				ID_BOARD -> TYPE_TEXT_BIG
				ID_DIAGNOSTICS -> TYPE_TEXT_BIG
				ID_MOVES -> TYPE_TEXT_SMALL
				ID_CLOCK -> TYPE_CLOCK
				else -> throw NotImplementedError()
			}
	}
}