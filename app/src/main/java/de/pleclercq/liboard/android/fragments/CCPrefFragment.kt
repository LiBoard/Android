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

package de.pleclercq.liboard.android.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import de.pleclercq.liboard.R
import de.pleclercq.liboard.android.adapters.TabPagerAdapter

@ExperimentalUnsignedTypes
class CCPrefFragment(private val adapter: TabPagerAdapter) :
	PreferenceFragmentCompat() {
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.clock_prefs, null)
	}

	override fun onStop() {
		adapter.makeClock()
		super.onStop()
	}
}