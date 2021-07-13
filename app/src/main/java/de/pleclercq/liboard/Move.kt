/*  Copyright (C) 2021  Philipp Leclercq

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version. */

package de.pleclercq.liboard

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import kotlin.math.abs

/**
 * Check if a move is a capture ("normal" or en passant).
 */
fun Board.isCapture(move: Move) = isNormalCapture(move) || isEnPassant(move)

/**
 * Check if a move is a "normal" (non en passant) capture.
 */
fun Board.isNormalCapture(move: Move) = getPiece(move.to) != Piece.NONE

// TODO implement 960 castling
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