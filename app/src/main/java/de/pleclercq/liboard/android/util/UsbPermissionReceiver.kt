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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager

@ExperimentalUnsignedTypes
internal class UsbPermissionReceiver(private val callback: Runnable) : BroadcastReceiver() {
	override fun onReceive(context: Context?, intent: Intent?) {
		if (intent != null && intent.action == ACTION
			&& intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
		)
			callback.run()
	}

	companion object {
		const val ACTION = "de.pleclercq.liboard.USB_PERMISSION_GRANTED"
	}
}