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

import androidx.preference.PreferenceManager
import com.github.bhlangonijr.chesslib.Side
import de.pleclercq.liboard.MainActivity
import de.pleclercq.liboard.TabPagerAdapter
import de.pleclercq.liboard.chessclock.ChessClock
import de.pleclercq.liboard.fragments.makeClock
import de.pleclercq.liboard.liboard.LiBoard
import de.pleclercq.liboard.liboard.LiBoardEvent
import de.pleclercq.liboard.liboard.LiBoardEventHandler
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@ExperimentalUnsignedTypes
class ClockManager(
	private val activity: MainActivity,
	private val liBoard: LiBoard,
	private val adapter: TabPagerAdapter
) :
	LiBoardEventHandler {
	private val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
	var clock = prefs.makeClock()
		set(value) {
			liBoard.clockMove = prefs.getString("clock_mode", "") == "clock-move"
			field = value
		}
	private val clockMode
		get() = prefs.getString("clock_mode", "")!!
	private val observeMoves
		get() = clockMode.matches(Regex("synchronized|stopwatch"))
	private val scheduler = Executors.newScheduledThreadPool(1)
	private val runnable = Runnable { onTick() }
	private var handle: ScheduledFuture<*>? = null

	override fun onEvent(e: LiBoardEvent) {
		when (e.type) {
			LiBoardEvent.TYPE_NEW_PHYSICAL_POS -> adapter.updateItems()
			LiBoardEvent.TYPE_GAME_START -> {
				if (observeMoves || liBoard.clockMove)
					clock.reset()
				adapter.updateItems()
			}
			LiBoardEvent.TYPE_MOVE -> {
				if (observeMoves) {
					clock.side = if (liBoard.board.sideToMove == Side.WHITE) ChessClock.WHITE else ChessClock.BLACK
					liBoard.board.sideToMove
					startClock()
				}
				adapter.updateItems()
			}
		}
	}

	internal fun onClockTapped(side: Int) {
		if (clockMode == "independent" ||
			(clockMode == "clock-move" && liBoard.board.sideToMove.ordinal == side && liBoard.tryClockSwitch())
		)
			clock.side = side.inverted()
		startClock()
	}

	private fun startClock() {
		if (clock.flagged == null) {
			clock.running = true
			if (handle == null || handle!!.isCancelled)
				handle = scheduler.scheduleAtFixedRate(runnable, 0, CLOCK_REFRESH_RATE, TimeUnit.MILLISECONDS)
		}
	}

	private fun onTick() {
		activity.runOnUiThread { adapter.updateItems() }
		if (!clock.running) handle?.cancel(true)
	}

	private fun Int.inverted() = if (this == ChessClock.WHITE) ChessClock.BLACK else ChessClock.WHITE

	companion object {
		const val CLOCK_REFRESH_RATE = 100L
	}
}