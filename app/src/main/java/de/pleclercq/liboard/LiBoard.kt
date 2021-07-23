/*  Copyright (C) 2021  Philipp Leclercq

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version. */

package de.pleclercq.liboard

import android.app.Activity
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.game.Game
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveList
import java.util.*

/**
 * A class handling everything related to the board.
 * Handles the serial connection, incoming data and move validation.
 *
 * @param activity The [Activity] this object belongs to. Required for access to system services, permissions etc.
 * @param eventHandler A [LiBoardEventHandler] that is used to react to game starts, moves and connection related events.
 *
 * @property knownPosition The physical position matching [board].
 * @property physicalPosition The current physical position.
 * @property liftedPieces All pieces that were lifted (temporarily or permanently) since the last move.
 */
@ExperimentalUnsignedTypes
internal class LiBoard(private val activity: Activity, private var eventHandler: LiBoardEventHandler) {
    val isConnected get() = connection != null
    lateinit var board: Board
        private set
    internal var physicalPosition = PhysicalPosition.STARTING_POSITION
        private set
    private val moveList = MoveList()
    private val liftedPieces = HashSet<Int>()
    private var knownPosition = PhysicalPosition.STARTING_POSITION
    private var connection: Connection? = null

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
        eventHandler.onNewPhysicalPosition()
        physicalPosition = position
        if (position == PhysicalPosition.STARTING_POSITION) {
            newGame()
            updateKnownPosition()
            eventHandler.onGameStart()
        } else {
            liftedPieces.addAll(knownPosition.occupiedSquares.minus(this.physicalPosition.occupiedSquares))
            generateMove()
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
     * and calls [eventHandler]'s [LiBoardEventHandler.onMove].
     */
    private fun onMove(move: Move) {
        updateKnownPosition()
        moveList.addLast(move)
        eventHandler.onMove()
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

    /**
     * Export a [Game] with the [Move]s in [moveList].
     */
    fun exportGame(): Game {
        val game = Game()
        game.halfMoves = moveList
        return game
    }
    //endregion

    //region Connection
    /**
     * Creates a new [Connection].
     */
    fun connect() {
        if (isConnected) disconnect()
        connection = Connection(activity, this)
        eventHandler.onConnect()
    }

    /**
     * Closes and unregisters the current [Connection].
     */
    fun disconnect() {
        connection?.close()
        connection = null
        eventHandler.onDisconnect()
    }
    //endregion
}