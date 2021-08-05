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

import android.os.Handler
import android.os.Looper
import androidx.preference.PreferenceManager
import com.github.bhlangonijr.chesslib.Side
import de.pleclercq.liboard.MainActivity
import de.pleclercq.liboard.TabPagerAdapter
import de.pleclercq.liboard.chessclock.ChessClock
import de.pleclercq.liboard.fragments.makeClock
import de.pleclercq.liboard.liboard.LiBoard
import de.pleclercq.liboard.liboard.LiBoardEvent
import de.pleclercq.liboard.liboard.LiBoardEventHandler

@ExperimentalUnsignedTypes
class ClockManager(
	activity: MainActivity,
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
	private val handler = Handler(Looper.getMainLooper())
	private val runnable = Runnable { onTick() }

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
		if (!observeMoves && liBoard.tryClockSwitch())
			clock.side = side.inverted()
		startClock()
	}

	private fun startClock() {
		if (clock.flagged == null) {
			clock.running = true
			handler.post(runnable)
		}
	}

	private fun onTick() {
		adapter.updateItems()
		if (clock.running) {
			if (clock.flagged == null) handler.postDelayed(runnable, TabPagerAdapter.CLOCK_REFRESH_RATE)
		}
	}

	private fun Int.inverted() = if (this == ChessClock.WHITE) ChessClock.BLACK else ChessClock.WHITE
}