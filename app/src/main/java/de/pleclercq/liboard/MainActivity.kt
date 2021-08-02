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

package de.pleclercq.liboard

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import de.pleclercq.liboard.databinding.ActivityMainBinding
import de.pleclercq.liboard.fragments.CreditsFragment
import de.pleclercq.liboard.fragments.TabbedFragment


@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity() {
	private val tabbedFragment = TabbedFragment(this)
	private val creditsFragment = CreditsFragment(this)
	private lateinit var binding: ActivityMainBinding


	//region Activity lifecycle
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		supportFragmentManager.beginTransaction().add(R.id.main_fragment_container_view, tabbedFragment).commit()
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.activity_main, menu)
		return true
	}
	//endregion

	//region UI events
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.credits -> supportFragmentManager.beginTransaction()
				.replace(R.id.main_fragment_container_view, creditsFragment).addToBackStack("credits").commit()

			else -> return false
		}
		return true
	}
	//endregion
}
