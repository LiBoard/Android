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

package de.pleclercq.liboard.android

import android.app.Application
import androidx.preference.PreferenceManager
import de.pleclercq.liboard.android.util.setTheme

@ExperimentalUnsignedTypes
@Suppress("unused")
class Application : Application() {
	override fun onCreate() {
		super.onCreate()
		setTheme(PreferenceManager.getDefaultSharedPreferences(this))
	}
}