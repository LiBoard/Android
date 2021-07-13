package de.pleclercq.liboard

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.Move
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.Closeable
import java.util.*
import java.util.concurrent.Executors

@ExperimentalUnsignedTypes
internal class LiBoard(private val activity: Activity, private var eventHandler: EventHandler) {
    var board = Board()
    private var knownPosition = Position.STARTING_POSITION
    private var physicalPosition = Position.STARTING_POSITION
    private val liftedPieces = HashSet<Int>()
    private var connection: Connection? = null
    val isConnected get() = connection != null

    //region Position
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

    private fun makeMove(move: Move): Boolean {
        if (board.doMove(move)) {
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
        val temporarilyLiftedPieces = liftedPieces.intersect(physicalPosition.occupiedSquares)

        if (disappearances.size == 1 && appearances.size == 1) {
            // normal move
            val m = board.findMove(disappearances.first(), appearances.first())
            return m != null && !board.isCastling(m) && !board.isCapture(m) && makeMove(m)
        } else if (disappearances.size == 1 && appearances.isEmpty() && temporarilyLiftedPieces.isNotEmpty()) {
            // "normal" capture (not e.p.)
            for (to in temporarilyLiftedPieces) {
                val m = board.findMove(disappearances.first(), to)
                if (m != null && board.isNormalCapture(m) && makeMove(m))
                    return true
            }
            return false
        } else if (disappearances.size == 2 && appearances.size == 1) {
            // en passant
            for (from in disappearances) {
                for (to in appearances) {
                    val m = board.findMove(from, to)
                    if (m != null && board.isEnPassant(m) && makeMove(m))
                        return true
                }
            }
            return false
        } else if (disappearances.size == 2 && appearances.size == 2) {
            // castling
            for (from in disappearances) {
                for (to in appearances) {
                    val m = board.findMove(from, to)
                    if (m != null && board.isCastling(m) && makeMove(m))
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
            liftedPieces.clear()
            board = Board()
            eventHandler.onGameStart()
        } else {
            liftedPieces.addAll(knownPosition.occupiedSquares.minus(physicalPosition.occupiedSquares))
            generateMove()
        }
    }
    //endregion

    //region Connection
    private class Connection(activity: Activity, val liboard: LiBoard) :
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
            liboard.disconnect()
        }

        companion object {
            val LOG_TAG = Connection::class.simpleName
            val prober = UsbSerialProber(ProbeTable().addProduct(0x1b4f, 0x9206, CdcAcmSerialDriver::class.java))
        }

    }

    fun connect() {
        if (isConnected) disconnect()
        connection = Connection(activity, this)
        eventHandler.onConnect()
    }

    fun disconnect() {
        connection?.close()
        connection = null
        eventHandler.onDisconnect()
    }
    //endregion

    //region Interfaces&Exceptions
    internal interface EventHandler {
        fun onGameStart()
        fun onMove()
        fun onConnect()
        fun onDisconnect()
    }

    internal abstract class ConnectionException(str: String) : Exception(str)
    internal class MissingDriverException(str: String) : ConnectionException(str)
    internal class UsbPermissionException(str: String) : ConnectionException(str)
    internal class LiBoardInvalidPositionError(str: String) : Exception(str)
    //endregion
}