package de.pleclercq.liboard

/**
 * Represents a physical board position.
 *
 * @property bitboard The raw board data as a [ULong].
 * @property occupiedSquares A [Set] of the indices of the occupied squares.
 */
@ExperimentalUnsignedTypes
internal class PhysicalPosition(val bitboard: ULong) {
    val occupiedSquares = ((0..63).filter { bitboard and (1UL shl it) != 0UL }).toSet()

    override fun equals(other: Any?): Boolean {
        return if (other is PhysicalPosition) {
            bitboard == other.bitboard
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        return bitboard.hashCode()
    }

    companion object {
        val STARTING_POSITION = PhysicalPosition(0xFFFF00000000FFFFUL)
    }
}