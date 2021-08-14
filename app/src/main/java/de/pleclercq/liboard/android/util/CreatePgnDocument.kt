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

package de.pleclercq.liboard.android.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

internal class CreatePgnDocument : ActivityResultContract<String, Uri>() {
	override fun createIntent(context: Context, input: String?) = Intent().apply {
		action = Intent.ACTION_CREATE_DOCUMENT
		type = "application/x-chess-pgn"
		putExtra(Intent.EXTRA_TITLE, input)
	}

	override fun parseResult(resultCode: Int, intent: Intent?) =
		if (intent == null || resultCode != Activity.RESULT_OK) null
		else intent.data
}