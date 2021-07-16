package de.pleclercq.liboard

import java.util.HashSet

/**
 * Represents a physical board position.
 *
 * @property bytes The raw board data as a [List].
 * @property occupiedSquares A [Set] of the indices of the occupied squares.
 */
@ExperimentalUnsignedTypes
internal class PhysicalPosition(_bytes: Collection<UByte>) {
    val occupiedSquares: Set<Int>
    val bytes: List<UByte>

    init {
        if (_bytes.size != 8)
            throw LiBoard.LiBoardInvalidPositionError("Position was ${_bytes.size} bytes, expected 8")

        bytes = _bytes.toList()

        val s = HashSet<Int>()
        _bytes.forEachIndexed { file, byte ->
            for (rank in 0 until 8) {
                if ((byte and (1u shl rank).toUByte()).toInt() != 0) {
                    s.add(8 * rank + (7 - file))
                }
            }
        }
        occupiedSquares = s.toSet()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is PhysicalPosition) {
            bytes == other.bytes
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        return bytes.hashCode()
    }

    companion object {
        val STARTING_POSITION = PhysicalPosition(ubyteArrayOf(0xC3U, 0xC3U, 0xC3U, 0xC3U, 0xC3U, 0xC3U, 0xC3U, 0xC3U))
    }
}