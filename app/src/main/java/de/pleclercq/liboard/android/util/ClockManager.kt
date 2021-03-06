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

package de.pleclercq.liboard.android.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.github.bhlangonijr.chesslib.Side
import de.pleclercq.liboard.android.adapters.TabPagerAdapter
import de.pleclercq.liboard.chessclock.ChessClock
import de.pleclercq.liboard.chessclock.DelayClock
import de.pleclercq.liboard.chessclock.Stopwatch
import de.pleclercq.liboard.chessclock.TimeControl
import de.pleclercq.liboard.liboard.Event
import de.pleclercq.liboard.liboard.Event.*
import de.pleclercq.liboard.liboard.EventHandler
import de.pleclercq.liboard.liboard.LiBoard
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@ExperimentalUnsignedTypes
class ClockManager(
	private val context: Context,
	private val adapter: TabPagerAdapter
) :
	EventHandler {
	private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
	var clock = prefs.makeClock()
		set(value) {
			LiBoard.clockMove = prefs.getString("clock_mode", "") == "clock-move"
			field = value
		}
	private val clockMode get() = prefs.getString("clock_mode", "")!!
	private val observeMoves get() = clockMode != "independent"
	private val scheduler = Executors.newScheduledThreadPool(1)
	private val runnable = Runnable { onTick() }
	private var handle: ScheduledFuture<*>? = null
	private val refreshDelay get() = 1000L / prefs.getString("refresh_rate", "100")!!.toInt()

	override fun onEvent(e: Event) {
		when (e) {
			NEW_PHYSICAL_POS -> adapter.updateItems()
			GAME_START -> {
				if (observeMoves || LiBoard.clockMove)
					clock.reset()
				adapter.updateItems()
			}
			MOVE -> {
				if (observeMoves) {
					clock.side = if (LiBoard.board.sideToMove == Side.WHITE) ChessClock.WHITE else ChessClock.BLACK
					if (LiBoard.board.isMated || LiBoard.board.isDraw) clock.running = false
					else startClock()
				}
				adapter.updateItems()
			}
			TAKEBACK -> {
				if (observeMoves)
					clock.side = if (LiBoard.board.sideToMove == Side.WHITE) ChessClock.WHITE else ChessClock.BLACK
				adapter.updateItems()
			}
			else -> return
		}
	}

	internal fun onClockTapped(side: Int) {
		if (clockMode == "independent")
			clock.side = side.inverted()
		else if (clockMode == "clock-move" && side == LiBoard.board.sideToMove.ordinal)
			LiBoard.tryClockSwitch()
		startClock()
	}

	internal fun makeClock() {
		clock = prefs.makeClock()
	}

	private fun startClock() {
		if (clock.flagged == null) {
			clock.running = true
			if (handle == null || handle!!.isCancelled)
				handle = scheduler.scheduleWithFixedDelay(runnable, 0, refreshDelay, TimeUnit.MILLISECONDS)
		}
	}

	private fun onTick() {
		(context as Activity).runOnUiThread { adapter.updateItems() }
		if (!clock.running) handle?.cancel(true)
	}

	private fun Int.inverted() = if (this == ChessClock.WHITE) ChessClock.BLACK else ChessClock.WHITE

	private fun SharedPreferences.makeClock() = when (getString("clock_mode", "")) {
		"stopwatch" -> Stopwatch()
		else -> {
			val colors = arrayOf("white", "black")
			val timeControl = TimeControl(
				colors.map { getString("tc_init_$it", "0")!!.toInt() * 60 }.toIntArray(),
				colors.map { getString("tc_inc_$it", "0")!!.toInt() }.toIntArray()
			)
			when (getString("tc_type", "")) {
				"increment" -> ChessClock(timeControl)
				else -> DelayClock(timeControl)
			}
		}
	}
}