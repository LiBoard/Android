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

package de.pleclercq.liboard.liboard

/**
 * Handler for game starts, moves and connection related events.
 */
interface LiBoardEventHandler {
	fun onEvent(e: LiBoardEvent)
}

data class LiBoardEvent(val type: Int) {
	companion object {
		const val TYPE_CONNECT = 0
		const val TYPE_DISCONNECT = 1
		const val TYPE_NEW_PHYSICAL_POS = 2
		const val TYPE_GAME_START = 3
		const val TYPE_MOVE = 4
	}
}