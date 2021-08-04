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

import android.os.Bundle
import android.text.InputType
import androidx.preference.*
import de.pleclercq.liboard.R

class ChessClockPreferenceFragment : PreferenceFragmentCompat() {
	val prefs = preferenceManager.sharedPreferences
	val prefScreen = preferenceManager.createPreferenceScreen(context).apply {
		DropDownPreference(context).apply {
			key = "clock-mode"
			title = "Mode"
			entries = resources.getStringArray(R.array.clock_modes)
			entryValues = resources.getStringArray(R.array.clock_mode_values)
		}.let { addPreference(it) }
	}
	val tcCategory = PreferenceCategory(context).apply {
		title = "Time control"
		key = "tc-prefs"
		DropDownPreference(context).apply {
			key = "tc-type"
			title = "Clock"
			entries = resources.getStringArray(R.array.tc_types)
			entryValues = resources.getStringArray(R.array.clock_mode_values)
		}.let { addPreference(it) }
	}
	val tcParams = PreferenceCategory(context).apply {
		key = "tc-params"
		makeTcParams("")
	}
	val tcParamsOdds = PreferenceCategory(context).apply {
		key = "tc-params-odds"
		makeTcParams("-white")
		makeTcParams("-black")
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		for (g in arrayOf(prefScreen, tcCategory, tcParams, tcParamsOdds))
			g.children.forEach { it.setOnPreferenceChangeListener { pref, newVal -> onPrefChange(pref, newVal) } }
		if (prefs.getString("tc-clock-mode", "independent") != "stopwatch") {
			when (prefs.getString("tc-type", "increment")) {
				"odds" -> tcParamsOdds
				"delay" -> tcParams.apply { tcParams.findPreference<EditTextPreference>("tc-inc")!!.title = "Delay" }
				else -> tcParams.apply { findPreference<EditTextPreference>("tc-inc")!!.title = "Increment" }
			}.let { tcCategory.addPreference(it) }
			prefScreen.addPreference(tcCategory)
		}
		preferenceScreen = prefScreen
	}

	private fun onPrefChange(pref: Preference, newVal: Any?): Boolean {
		when (pref.key) {
			"clock-mode" -> when (newVal as String) {
				"stopwatch" -> prefScreen.removePreference(tcCategory)
				else -> prefScreen.addPreference(tcCategory)
			}
			"tc-type" -> when (newVal as String) {
				"odds" -> {
					tcCategory.removePreference(tcParams)
					tcCategory.addPreference(tcParamsOdds)
				}
				"delay" -> {
					tcCategory.removePreference(tcParamsOdds)
					tcCategory.addPreference(tcParams)
					tcParams.findPreference<EditTextPreference>("tc-inc")!!.title = "Delay"
				}
				else -> {
					tcCategory.removePreference(tcParamsOdds)
					tcCategory.addPreference(tcParams)
					tcParams.findPreference<EditTextPreference>("tc-inc")!!.title = "Increment"
				}
			}
		}
		return true
	}

	private fun PreferenceCategory.makeTcParams(suffix: String) {
		EditTextPreference(context).apply {
			key = "tc-init$suffix"
			title = "Initial time"
			setOnBindEditTextListener { it.inputType = InputType.TYPE_CLASS_NUMBER }
		}.let { addPreference(it) }
		DropDownPreference(context).apply {
			key = "tc-init-unit$suffix"
			entries = arrayOf("min", "s")
			entryValues = entries
		}.let { addPreference(it) }
		EditTextPreference(context).apply {
			key = "tc-inc$suffix"
			title = "Increment"
			setOnBindEditTextListener { it.inputType = InputType.TYPE_CLASS_NUMBER }
		}.let { addPreference(it) }
		DropDownPreference(context).apply {
			key = "tc-inc-unit$suffix"
			entries = arrayOf("min", "s")
			entryValues = entries
		}.let { addPreference(it) }
	}
}