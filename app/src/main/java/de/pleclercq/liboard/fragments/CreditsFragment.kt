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
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.pleclercq.liboard.MainActivity
import de.pleclercq.liboard.R
import de.pleclercq.liboard.databinding.FragmentCreditsBinding

@ExperimentalUnsignedTypes
class CreditsFragment(private val activity: MainActivity) : Fragment() {
    private lateinit var binding: FragmentCreditsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreditsBinding.inflate(inflater, container, false)
        binding.closeCredits.setOnClickListener { activity.supportFragmentManager.popBackStack() }
        binding.creditsTextView.loadData(
            Base64.encodeToString(getString(R.string.credits_html).toByteArray(), Base64.NO_PADDING),
            "text/html",
            "base64"
        )
        return binding.root
    }
}