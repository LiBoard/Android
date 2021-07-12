package de.pleclercq.liboard

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import kotlin.math.abs

fun Board.isCapture(move: Move) = isNormalCapture(move) || isEnPassant(move)

fun Board.isNormalCapture(move: Move) = getPiece(move.from) != null

fun Board.isCastling(move: Move) =
    getPiece(move.from).pieceType == PieceType.KING && abs(move.from.ordinal - move.to.ordinal) == 2

fun Board.isEnPassant(move: Move) = getPiece(move.from).pieceType == PieceType.PAWN && enPassantTarget == move.to

fun Board.findMove(from: Square, to: Square) = legalMoves().firstOrNull { m -> m.from == from && m.to == to }
fun Board.findMove(from: Int, to: Int) = findMove(Square.squareAt(from), Square.squareAt(to))