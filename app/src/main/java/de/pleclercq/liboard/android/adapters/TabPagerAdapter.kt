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

package de.pleclercq.liboard.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import de.pleclercq.liboard.R
import de.pleclercq.liboard.chessclock.ChessClock.Companion.BLACK
import de.pleclercq.liboard.chessclock.ChessClock.Companion.WHITE
import de.pleclercq.liboard.chessclock.ClockSnapshot
import de.pleclercq.liboard.databinding.ChessclockBinding
import de.pleclercq.liboard.android.fragments.CCPrefFragment
import de.pleclercq.liboard.liboard.*
import de.pleclercq.liboard.android.util.ClockManager
import de.pleclercq.liboard.android.viewHolders.ClockHolder
import de.pleclercq.liboard.android.viewHolders.TextViewHolder
import de.pleclercq.liboard.android.viewHolders.ViewHolder

@ExperimentalUnsignedTypes
class TabPagerAdapter(private val context: Context, private val liBoard: LiBoard) :
	RecyclerView.Adapter<ViewHolder>() {
	internal val clockManager = ClockManager(context, liBoard, this)
	private var items = fetchItems()

	//region Adapter
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

	override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.updateContents(items[position].data)
	override fun getItemCount() = items.size
	override fun getItemViewType(position: Int) = items[position].type

	fun getTitle(position: Int) = items[position].title

	fun updateItems() {
		items = fetchItems().apply {
			indices.filter { get(it) != items[it] }.forEach { notifyItemChanged(it, true) }
		}
	}

	private fun fetchItems(): List<Item> {
		return mutableListOf(
			Item("Board", TYPE_TEXT_BIG, liBoard.board.toString()),
			Item("Moves", TYPE_TEXT_SMALL, Game(liBoard).toPgn()),
			Item("Clock", TYPE_CLOCK, ClockSnapshot(clockManager.clock))
		).apply {
			if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("debug", false))
				add(Item("Sensors", TYPE_TEXT_BIG, liBoard.physicalPosition.toString()))
		}
	}
	//endregion

	//region Clock
	fun makeClock() {
		clockManager.makeClock()
		updateItems()
	}

	//TODO move to ClockHolder
	private fun onClick(view: View) {
		when (view.id) {
			R.id.clock_black -> clockManager.onClockTapped(BLACK)
			R.id.clock_white -> clockManager.onClockTapped(WHITE)
			R.id.clock_stop -> clockManager.clock.running = false
			R.id.clock_reset -> clockManager.clock.reset()
			R.id.clock_settings -> (context as AppCompatActivity).supportFragmentManager.beginTransaction().apply {
				replace(R.id.main_fragment_container_view, CCPrefFragment(this@TabPagerAdapter))
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
	}

	private data class Item(val title: String, val type: Int, val data: Any)
}