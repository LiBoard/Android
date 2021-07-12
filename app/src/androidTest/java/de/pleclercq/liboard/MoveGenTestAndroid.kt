package de.pleclercq.liboard

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoveGenTestAndroid {
    @Test
    fun moveGenLegalNormal() = assertNotNull(Board().findMove(Square.E2, Square.E4))

    @Test
    fun moveGenIllegalNormal() = assertNull(Board().findMove(Square.A1, Square.E4))
}