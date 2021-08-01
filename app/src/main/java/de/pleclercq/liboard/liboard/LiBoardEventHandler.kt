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

package de.pleclercq.liboard.liboard

/**
 * Handler for game starts, moves and connection related events.
 */
interface LiBoardEventHandler {
    /**
     * Called when a new game starts.
     */
    fun onGameStart()

    /**
     * Called when a legal move is detected.
     */
    fun onMove()

    /**
     * Called when the physical board position changes.
     */
    fun onNewPhysicalPosition()

    /**
     * Called when a LiBoard is connected.
     */
    fun onConnect()

    /**
     * Called when the LiBoard is disconnected.
     */
    fun onDisconnect()
}