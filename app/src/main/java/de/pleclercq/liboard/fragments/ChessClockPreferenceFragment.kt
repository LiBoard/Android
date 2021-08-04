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

package de.pleclercq.liboard.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import de.pleclercq.liboard.MainActivity
import de.pleclercq.liboard.R
import de.pleclercq.liboard.TabPagerAdapter
import de.pleclercq.liboard.chessclock.ChessClock
import de.pleclercq.liboard.chessclock.DelayClock
import de.pleclercq.liboard.chessclock.Stopwatch
import de.pleclercq.liboard.chessclock.TimeControl

@ExperimentalUnsignedTypes
class ChessClockPreferenceFragment(private val activity: MainActivity, private val adapter: TabPagerAdapter) :
	PreferenceFragmentCompat() {
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.clock_prefs, null)
	}

	override fun onStop() {
		adapter.clock = preferenceManager.sharedPreferences.makeClock()
		adapter.updateItems()
		super.onStop()
	}
}

fun SharedPreferences.makeClock() = when (getString("clock_mode", "")) {
	"stopwatch" -> Stopwatch()
	else -> {
		val colors = arrayOf("white", "black")
		val timeControl = TimeControl(
			colors.map { getString("tc_init_$it", "0")!!.toInt() }.toIntArray(),
			colors.map { getString("tc_inc_$it", "0")!!.toInt() }.toIntArray()
		)
		when (getString("tc_type", "")) {
			"increment" -> ChessClock(timeControl)
			else -> DelayClock(timeControl)
		}
	}
}