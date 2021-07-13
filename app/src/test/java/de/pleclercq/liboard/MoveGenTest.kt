package de.pleclercq.liboard

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import org.junit.Test
import org.junit.Assert.*

class MoveGenTest {
    @Test
    fun moveGenLegalNormal() {
        val b = Board()
        assertNotNull(b.findMove(Square.E2, Square.E4))
    }

    @Test
    fun moveGenIllegalNormal() {
        val b = Board()
        assertNull(b.findMove(Square.A1, Square.E4))
    }
}