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
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import de.pleclercq.liboard.R

class ChessClockPreferenceFragment : PreferenceFragmentCompat() {
	val prefScreen = preferenceManager.createPreferenceScreen(context).apply {
		DropDownPreference(context).apply {
			key = "clock-mode"
			title = "Mode"
			entries = resources.getStringArray(R.array.clock_modes)
			entryValues = resources.getStringArray(R.array.clock_mode_values)
		}.let { addPreference(it) }
	}
	val timeControlCategory = PreferenceCategory(context).apply {
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
		EditTextPreference(context).apply {
			key = "tc-init"
			title = "Initial time"
			setOnBindEditTextListener { it.inputType = InputType.TYPE_CLASS_NUMBER }
		}.let { addPreference(it) }
		DropDownPreference(context).apply {
			key = "tc-init-unit"
			entries = arrayOf("min", "s")
			entryValues = entries
		}.let { addPreference(it) }
		EditTextPreference(context).apply {
			key = "tc-inc"
			title = "Initial time"
			setOnBindEditTextListener { it.inputType = InputType.TYPE_CLASS_NUMBER }
		}.let { addPreference(it) }
		DropDownPreference(context).apply {
			key = "tc-inc-unit"
			entries = arrayOf("min", "s")
			entryValues = entries
		}.let { addPreference(it) }
	}

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		TODO("Not yet implemented")
	}
}