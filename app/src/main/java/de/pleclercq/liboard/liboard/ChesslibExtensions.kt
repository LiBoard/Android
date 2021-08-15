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

import com.github.bhlangonijr.chesslib.*
import com.github.bhlangonijr.chesslib.game.*
import com.github.bhlangonijr.chesslib.game.Event
import com.github.bhlangonijr.chesslib.move.Move
import kotlin.math.abs

//region Board
/**
 * Check if a move is a capture ("normal" or en passant).
 */
fun Board.isCapture(move: Move) = isNormalCapture(move) || isEnPassant(move)

/**
 * Check if a move is a "normal" (non en passant) capture.
 */
fun Board.isNormalCapture(move: Move) = getPiece(move.to) != Piece.NONE

/**
 * Check if a move is castling.
 * This checks for a king moving two squares along one rank. 960 castling is not (currently) supported.
 */
fun Board.isCastling(move: Move) =
	getPiece(move.from).pieceType == PieceType.KING && abs(move.from.ordinal - move.to.ordinal) == 2

/**
 * Checks if a move is en passant.
 */
fun Board.isEnPassant(move: Move) = getPiece(move.from).pieceType == PieceType.PAWN && enPassant == move.to

/**
 * Tries to find the first legal move matching the given [from] and [to] [Square]s.
 *
 * In case of a promotion, [Board.legalMoves] always returns the promotion to a Queen first.
 */
fun Board.findMove(from: Square, to: Square) = legalMoves().firstOrNull { m -> m.from == from && m.to == to }

/**
 * Tries to find the first legal move matching the given [from] and [to] [Square] indices.
 *
 * In case of a promotion, [Board.legalMoves] always returns the promotion to a Queen first.
 */
fun Board.findMove(from: Int, to: Int) = findMove(Square.squareAt(from), Square.squareAt(to))
//endregion

//region Game
/**
 * Creates a [Game] no information.
 */
fun Game(): Game {
	return Game("",
		Round(Event().apply {
			name = ""
			site = ""
			startDate = ""
		}).apply { number = 1 }).apply {
		whitePlayer = GenericPlayer("", "")
		blackPlayer = GenericPlayer("", "")
		result = GameResult.ONGOING
		plyCount = ""
		moveText = StringBuilder()
	}
}

@ExperimentalUnsignedTypes
fun Game(liBoard: LiBoard) = Game().apply {
	halfMoves = liBoard.getMoves()
	board = liBoard.board
	result = if (board.isDraw) {
		GameResult.DRAW
	} else if (board.isMated) {
		if (board.sideToMove == Side.WHITE) GameResult.BLACK_WON else GameResult.WHITE_WON
	} else {
		GameResult.ONGOING
	}
}

fun Game.toPgn(): String = this.toPgn(true, true)
//endregion