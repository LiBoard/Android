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

import android.content.Context
import android.util.Log
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveList
import de.pleclercq.liboard.liboard.LiBoard.board
import de.pleclercq.liboard.liboard.LiBoard.knownPosition
import de.pleclercq.liboard.liboard.LiBoard.liftedPieces
import de.pleclercq.liboard.liboard.LiBoard.physicalPosition
import de.pleclercq.liboard.liboard.LiBoardEvent.Companion.TYPE_CONNECT
import de.pleclercq.liboard.liboard.LiBoardEvent.Companion.TYPE_DISCONNECT
import de.pleclercq.liboard.liboard.LiBoardEvent.Companion.TYPE_GAME_START
import de.pleclercq.liboard.liboard.LiBoardEvent.Companion.TYPE_MOVE
import de.pleclercq.liboard.liboard.LiBoardEvent.Companion.TYPE_NEW_PHYSICAL_POS
import de.pleclercq.liboard.liboard.LiBoardEvent.Companion.TYPE_TAKEBACK

/**
 * A class handling everything related to the board.
 * Handles the serial connection, incoming data and move validation.
 *
 * @property knownPosition The physical position matching [board].
 * @property physicalPosition The current physical position.
 * @property liftedPieces All pieces that were lifted (temporarily or permanently) since the last move.
 */
@ExperimentalUnsignedTypes
object LiBoard {
	internal val eventHandlers = mutableListOf<LiBoardEventHandler>()
	val isConnected get() = connection != null
	lateinit var board: Board
		private set
	internal var physicalPosition = PhysicalPosition.STARTING_POSITION
		private set
	private val moveList = MoveList()
	private val liftedPieces = HashSet<Int>()
	private var knownPosition = PhysicalPosition.STARTING_POSITION
	private var connection: Connection? = null
	internal var clockMove = false

	init {
		newGame()
	}

	//region Position
	/**
	 * Tries to find a move that matches the difference between [knownPosition] and [physicalPosition].
	 * Makes the move if one was found.
	 *
	 * @return whether a move was found
	 */
	private fun generateMove(): Boolean {
		val disappearances = knownPosition.occupiedSquares.minus(physicalPosition.occupiedSquares)
		val appearances = physicalPosition.occupiedSquares.minus(knownPosition.occupiedSquares)
		val temporarilyLiftedPieces = liftedPieces.intersect(physicalPosition.occupiedSquares)

		if (disappearances.size == 1 && appearances.size == 1) {
			// normal move
			val m = board.findMove(disappearances.first(), appearances.first())
			return m != null && !board.isCastling(m) && !board.isCapture(m) && tryMove(m)
		} else if (disappearances.size == 1 && appearances.isEmpty() && temporarilyLiftedPieces.isNotEmpty()) {
			// "normal" capture (not e.p.)
			for (to in temporarilyLiftedPieces) {
				val m = board.findMove(disappearances.first(), to)
				if (m != null && board.isNormalCapture(m) && tryMove(m))
					return true
			}
			return false
		} else if (disappearances.size == 2 && appearances.size == 1) {
			// en passant
			for (from in disappearances) {
				for (to in appearances) {
					val m = board.findMove(from, to)
					if (m != null && board.isEnPassant(m) && tryMove(m))
						return true
				}
			}
			return false
		} else if (disappearances.size == 2 && appearances.size == 2) {
			// castling
			for (from in disappearances) {
				for (to in appearances) {
					val m = board.findMove(from, to)
					if (m != null && board.isCastling(m) && tryMove(m))
						return true
				}
			}
			return false
		}
		return false
	}

	/**
	 * Gets called when a new physical board position comes in.
	 * Resets the board to the starting position if necessary
	 * or tries to find a legal move matching the position otherwise.
	 */
	internal fun onNewPhysicalPosition(position: PhysicalPosition) {
		broadcastEvent(LiBoardEvent(TYPE_NEW_PHYSICAL_POS))
		physicalPosition = position
		if (position == PhysicalPosition.STARTING_POSITION) {
			newGame()
			updateKnownPosition()
			broadcastEvent(LiBoardEvent(TYPE_GAME_START))
		} else {
			liftedPieces.addAll(knownPosition.occupiedSquares.minus(this.physicalPosition.occupiedSquares))
			if (!clockMove) generateMove()
		}
	}

	/**
	 * Tries a move.
	 * Calls [onMove] and returns true if successful.
	 */
	private fun tryMove(move: Move): Boolean {
		if (board.doMove(move)) {
			onMove(move)
			return true
		}
		return false
	}

	/**
	 * Called when a new [Move] is detected.
	 * Updates the [knownPosition], adds the [move] to the [moveList]
	 * and broadcasts the corresponding Event.
	 */
	private fun onMove(move: Move) {
		updateKnownPosition()
		moveList.addLast(move)
		broadcastEvent(LiBoardEvent(TYPE_MOVE))
	}

	/**
	 * Creates a new [Board] and clears the [moveList].
	 */
	private fun newGame() {
		board = Board()
		moveList.clear()
	}

	/**
	 * Sets [knownPosition] tp [physicalPosition] and clears [liftedPieces].
	 */
	private fun updateKnownPosition() {
		knownPosition = physicalPosition
		liftedPieces.clear()
	}

	fun getMoves() = MoveList(moveList)

	fun takeback() {
		try {
			moveList.removeLast()
			board.undoMove()
		} catch (e: NoSuchElementException) {
			Log.w("takeback", "Takeback was attempted without any moves played")
		}
		knownPosition = PhysicalPosition(board)
		liftedPieces.clear()
		broadcastEvent(LiBoardEvent(TYPE_TAKEBACK))
	}
	//endregion

	//region Connection
	/**
	 * Creates a new [Connection].
	 */
	fun connect(context: Context) {
		if (isConnected) disconnect()
		connection = Connection(context)
		broadcastEvent(LiBoardEvent(TYPE_CONNECT))
	}

	/**
	 * Closes and unregisters the current [Connection].
	 */
	fun disconnect() {
		if (connection != null) {
			connection?.close()
			connection = null
			broadcastEvent(LiBoardEvent(TYPE_DISCONNECT))
		}
	}
	//endregion

	private fun broadcastEvent(e: LiBoardEvent) = eventHandlers.forEach { it.onEvent(e) }

	fun tryClockSwitch() = if (!clockMove) true else generateMove()
}