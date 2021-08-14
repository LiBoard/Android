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

package de.pleclercq.liboard.android.viewHolders

import android.content.Context
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.TextView
import java.io.InvalidClassException

class TextViewHolder(context: Context, textSize: Float = 30F, textAlignment: Int = TextView.TEXT_ALIGNMENT_CENTER) :
	ViewHolder(TextView(context).apply {
		this.textSize = textSize
		this.textAlignment = textAlignment
		this.layoutParams = LAYOUT_PARAMS
		this.typeface = Typeface.MONOSPACE
	}) {
	private val view = itemView as TextView

	override fun updateContents(data: Any) {
		if (data is String) view.text = data
		else throw InvalidClassException("Expected String, got ${data::class.simpleName}")
	}

	companion object {
		val LAYOUT_PARAMS = ViewGroup.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT
		)
	}
}