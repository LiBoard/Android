package de.pleclercq.liboard

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import org.junit.Test

class MoveTypeTest {
    @Test
    fun moveValidation(){
        val b = Board()
        val m = b.findMove(Square.E2, Square.E4)!!
        assert(!b.isCastling(m))
        assert(!b.isNormalCapture(m))
        assert(!b.isEnPassant(m))
        assert(!b.isCapture(m))
    }
}