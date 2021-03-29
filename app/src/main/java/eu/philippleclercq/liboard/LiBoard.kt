package eu.philippleclercq.liboard

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.Closeable
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashSet

@ExperimentalUnsignedTypes
internal class LiBoard(activity: Activity, var eventHandler: EventHandler) : Closeable {
    private val connection = Connection(activity, this)
    var board = Board()
    private var knownPosition = Position.STARTING_POSITION
    private var physicalPosition = Position.STARTING_POSITION
    private val liftedPieces = HashSet<Int>()

    private fun makeMove(move: Move): Boolean {
        if (board.doMove(move, true)) {
            knownPosition = physicalPosition
            liftedPieces.clear()
            eventHandler.onMove()
            return true
        }
        return false
    }

    private fun generateMove(): Boolean {
        val disappearances = knownPosition.occupiedSquares.minus(physicalPosition.occupiedSquares)
        val appearances = physicalPosition.occupiedSquares.minus(knownPosition.occupiedSquares)
        val temporarilyLiftedPieces = liftedPieces.minus(physicalPosition.occupiedSquares)

        if (disappearances.size == 1 && appearances.size == 1) {
            // normal move
            // TODO check for castling or captures, add promotions
            return makeMove(Move(disappearances.first(), appearances.first()))
        } else if (disappearances.size == 1 && appearances.isEmpty() && temporarilyLiftedPieces.isNotEmpty()) {
            // capture
            // TODO ensure capture, add promotions
            for (to in temporarilyLiftedPieces) {
                if (makeMove(Move(disappearances.first(), to)))
                    return true
            }
            return false
        } else if (disappearances.size == 2 && appearances.size == 1) {
            // en passant
            // TODO ensure en passant
            for (from in disappearances) {
                for (to in appearances) {
                    if (makeMove(Move(from, to)))
                        return true
                }
            }
            return false
        } else if (disappearances.size == 2 && appearances.size == 2) {
            // castling
            // TODO ensure castling
            for (from in disappearances) {
                for (to in appearances) {
                    if (makeMove(Move(from, to)))
                        return true
                }
            }
            return false
        }
        return false
    }

    private fun onNewPosition(position: Position) {
        physicalPosition = position
        if (position == Position.STARTING_POSITION) {
            knownPosition = position
            board = Board()
            eventHandler.onGameStart()
            liftedPieces.clear()
        } else {
            liftedPieces.addAll(knownPosition.occupiedSquares.minus(physicalPosition.occupiedSquares))
            generateMove()
        }
    }

    private fun onDisconnect() {
        eventHandler.onDisconnect()
    }

    override fun close() {
        connection.close()
    }

    private class Connection(val activity: Activity, val liboard: LiBoard) :
        Closeable, SerialInputOutputManager.Listener {
        private val usbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager
        private val port: UsbSerialPort
        private val data = LinkedList<UByte>()

        init {
            val availableDrivers = prober.findAllDrivers(usbManager)
            Log.d("serialConnect", "Drivers: $availableDrivers")
            if (availableDrivers.isEmpty()) throw MissingDriverException("No drivers available")

            // Open a connection to the first available driver.
            val driver = availableDrivers.first()
            val connection = usbManager.openDevice(driver.device)
            if (connection == null) {
                usbManager.requestPermission(
                    driver.device, PendingIntent.getActivity(
                        activity, 0,
                        activity.intent, 0
                    )
                )
                throw UsbPermissionException("No Usb permission")
            }

            port = driver.ports.first()
            port.open(connection)
            port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            port.dtr = true

            Executors.newSingleThreadExecutor().submit(SerialInputOutputManager(port, this))
        }

        override fun close() {
            port.close()
        }

        override fun onNewData(_data: ByteArray) {
            Log.v(LOG_TAG, "New data: $_data")
            data.addAll(_data.toUByteArray())
            if (data.size >= 8) {
                val positionBytes = LinkedList<UByte>()
                for (i in 0 until 8) {
                    val b = data.poll()
                    if (b != null)
                        positionBytes.add(b)
                }
                liboard.onNewPosition(Position(positionBytes))
            }
        }

        override fun onRunError(e: java.lang.Exception) {
            Log.e(LOG_TAG, "onRunError")
            close()
            liboard.onDisconnect()
        }

        companion object {
            val LOG_TAG = Connection::class.simpleName
            val prober = UsbSerialProber(ProbeTable().addProduct(0x1b4f, 0x9206, CdcAcmSerialDriver::class.java))
        }

    }

    private class Position(_bytes: Collection<UByte>) {
        val occupiedSquares: Set<Int>
        val bytes: List<UByte>

        init {
            if (_bytes.size != 8)
                throw LiBoardInvalidPositionError("Position was ${_bytes.size} bytes, expected 8")

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
            return if (other is Position) {
                bytes == other.bytes
            } else super.equals(other)
        }

        override fun hashCode(): Int {
            return bytes.hashCode()
        }

        companion object {
            val STARTING_POSITION = Position(ubyteArrayOf(0xC3U, 0xC3U, 0xC3U, 0xC3U, 0xC3U, 0xC3U, 0xC3U, 0xC3U))
        }
    }

    internal interface EventHandler {
        fun onGameStart()
        fun onMove()
        fun onDisconnect()
    }

    internal open class ConnectionException(str: String) : Exception(str)
    internal class MissingDriverException(str: String) : ConnectionException(str)
    internal class UsbPermissionException(str: String) : ConnectionException(str)
    internal class LiBoardInvalidPositionError(str: String) : Exception(str)

    private fun Move(from: Int, to: Int) = Move(Square.squareAt(from), Square.squareAt(to))
}