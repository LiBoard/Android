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

package de.pleclercq.liboard.android.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.pleclercq.liboard.R
import de.pleclercq.liboard.android.fragments.TabbedFragment
import de.pleclercq.liboard.databinding.ActivityMainBinding

@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container_view, TabbedFragment()).commit()
	}
}
