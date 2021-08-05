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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.pleclercq.liboard.chessclock.ChessClock
import de.pleclercq.liboard.chessclock.ChessClock.Companion.BLACK
import de.pleclercq.liboard.chessclock.ChessClock.Companion.WHITE
import de.pleclercq.liboard.databinding.ChessclockBinding
import de.pleclercq.liboard.fragments.ChessClockPreferenceFragment
import de.pleclercq.liboard.liboard.*
import de.pleclercq.liboard.util.ClockHolder
import de.pleclercq.liboard.util.ClockManager
import de.pleclercq.liboard.util.TextViewHolder
import de.pleclercq.liboard.util.ViewHolder

@ExperimentalUnsignedTypes
class TabPagerAdapter(private val activity: MainActivity, private val liBoard: LiBoard) :
	RecyclerView.Adapter<ViewHolder>(), LiBoardEventHandler {
	private val clockManager = ClockManager(activity, liBoard, this)
	var clock
		get() = clockManager.clock
		set(value) {
			clockManager.clock = value
		}
	private var items = fetchItems()

	//region Adapter
	override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.updateContents(items[position].data)
	override fun getItemCount() = items.size
	override fun getItemViewType(position: Int) = items[position].type

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

	override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
		super.onBindViewHolder(holder, position, payloads)
	}

	fun getTitle(position: Int) = items[position].title

	fun updateItems() {
		val tmp = fetchItems()
		tmp.forEachIndexed { index, item ->
			// the payload makes the update way more efficient for some reason
			if (item.data is ChessClock || item != items[index]) notifyItemChanged(index, true)
		}
		items = tmp
	}

	private fun fetchItems() = arrayOf(
		Item("Board", TYPE_TEXT_BIG, liBoard.board.toString()),
		Item("Moves", TYPE_TEXT_SMALL, Game(liBoard).toPgn()),
		Item("Clock", TYPE_CLOCK, clock),
		Item("Sensors", TYPE_TEXT_BIG, liBoard.physicalPosition.toString())
	)
	//endregion

	//region Clock
	override fun onEvent(e: LiBoardEvent) = clockManager.onEvent(e)

	private fun onClick(view: View) {
		when (view.id) {
			R.id.clock_black -> clockManager.onClockTapped(BLACK)
			R.id.clock_white -> clockManager.onClockTapped(WHITE)
			R.id.clock_stop -> clock.running = false
			R.id.clock_reset -> clock.reset()
			R.id.clock_settings -> activity.supportFragmentManager.beginTransaction().apply {
				replace(R.id.main_fragment_container_view, ChessClockPreferenceFragment(this@TabPagerAdapter))
				addToBackStack("clock settings")
				commit()
			}
		}
		updateItems()
	}
	//endregion

	companion object {
		const val TYPE_TEXT_BIG = 0
		const val TYPE_TEXT_SMALL = 1
		const val TYPE_CLOCK = 2
		const val CLOCK_REFRESH_RATE = 50L
	}

	private data class Item(val title: String, val type: Int, val data: Any)
}