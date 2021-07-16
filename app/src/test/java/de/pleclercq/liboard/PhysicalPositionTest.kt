package de.pleclercq.liboard

import org.junit.Test

@ExperimentalUnsignedTypes
class PhysicalPositionTest {
    @Test
    fun startingPosOccupiedSquares() {
        val o = PhysicalPosition.STARTING_POSITION.occupiedSquares
        for (i in (0 until 16) + (48 until 64))
            assert(o.contains(i))
        for (i in 16 until 48)
            assert(!o.contains(i))
    }
}