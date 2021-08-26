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
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.yariksoffice.lingver.Lingver
import com.yariksoffice.lingver.store.PreferenceLocaleStore
import de.pleclercq.liboard.android.util.setTheme
import de.pleclercq.liboard.liboard.LiBoard
import java.util.*

@ExperimentalUnsignedTypes
@Suppress("unused")
class Application : Application() {
	override fun onCreate() {
		super.onCreate()
		init(PreferenceManager.getDefaultSharedPreferences(this))
	}

	private fun init(preferences: SharedPreferences) {
		Log.d("LiBoard", "App init")
		setTheme(preferences)
		LiBoard.updateMoveDelay(preferences)

		// set language
		val lang = preferences.getString("language", "en")!!
		Log.d("LiBoard", "lang: $lang")
		Lingver.init(this, lang)
		Log.d("LiBoard", "language: ${Lingver.getInstance().getLanguage()}")
		Log.d("LiBoard", "locale: ${Lingver.getInstance().getLocale()}")
	}
}