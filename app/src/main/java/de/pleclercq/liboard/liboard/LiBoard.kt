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
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveList
import de.pleclercq.liboard.liboard.Event.*
import de.pleclercq.liboard.liboard.LiBoard.liftedPieces
import de.pleclercq.liboard.liboard.LiBoard.physicalPosition

/**
 * A class handling everything related to the board.
 * Handles the serial connection, incoming data and move validation.
 *
 * @property physicalPosition The current physical position.
 * @property liftedPieces All pieces that were lifted (temporarily or permanently) since the last move.
 */
@ExperimentalUnsignedTypes
object LiBoard {
	internal val eventHandlers = mutableListOf<EventHandler>()
	internal var clockMove = false
	private var moveDelay = 0L // ms
	private var connection: Connection? = null
	private val handler = Handler(Looper.getMainLooper())
	private val generateMoveRunnable = Runnable { generateMove() }

	lateinit var board: Board private set
	internal var physicalPosition = PhysicalPosition.STARTING_POSITION
		private set
	private val liftedPieces = HashSet<Int>()
	private val moveList = MoveList()

	val isConnected get() = connection != null

	init {
		newGame()
	}

	//region Position
	/**
	 * Tries to find a move that matches the difference between the known position and [physicalPosition].
	 * Makes the move if one was found.
	 *
	 * @return whether a move was found
	 */
	private fun generateMove(): Boolean {
		val knownPosition = PhysicalPosition(board)
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
		handler.removeCallbacks(generateMoveRunnable)
		broadcastEvent(NEW_PHYSICAL_POS)
		physicalPosition = position
		if (position == PhysicalPosition.STARTING_POSITION) {
			newGame()
			liftedPieces.clear()
			broadcastEvent(GAME_START)
		} else {
			liftedPieces.addAll(PhysicalPosition(board).occupiedSquares.minus(this.physicalPosition.occupiedSquares))
			if (!clockMove) handler.postDelayed(generateMoveRunnable, moveDelay)
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
	 * Adds the [move] to the [moveList]
	 * and broadcasts the corresponding Event.
	 */
	private fun onMove(move: Move) {
		liftedPieces.clear()
		moveList.addLast(move)
		broadcastEvent(MOVE)
	}

	/**
	 * Creates a new [Board] and clears the [moveList].
	 */
	private fun newGame() {
		board = Board()
		moveList.clear()
	}

	fun getMoves() = MoveList(moveList)

	fun takeback() {
		try {
			moveList.removeLast()
			board.undoMove()
		} catch (e: NoSuchElementException) {
			Log.w("takeback", "Takeback was attempted without any moves played")
		}
		liftedPieces.clear()
		broadcastEvent(TAKEBACK)
	}
	//endregion

	//region Connection
	/**
	 * Creates a new [Connection].
	 */
	fun connect(context: Context) {
		if (isConnected) disconnect()
		connection = Connection(context)
		broadcastEvent(CONNECT)
	}

	/**
	 * Closes and unregisters the current [Connection].
	 */
	fun disconnect() {
		if (connection != null) {
			connection?.close()
			connection = null
			broadcastEvent(DISCONNECT)
		}
	}
	//endregion

	private fun broadcastEvent(e: Event) = eventHandlers.forEach { it.onEvent(e) }

	fun updateMoveDelay(prefs: SharedPreferences) {
		moveDelay = prefs.getString("move-delay", "0")!!.toLong()
	}

	fun tryClockSwitch() = if (!clockMove) true else generateMove()
}