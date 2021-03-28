package eu.philippleclercq.liboard

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.github.bhlangonijr.chesslib.Board
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
internal class LiBoard(activity: Activity, var gameEventHandler: GameEventHandler) : NewPositionHandler, Closeable {
    private val connection = Connection(activity, this)
    val handler = Handler(Looper.getMainLooper())
    val runnable = Runnable {
        this.generateMove()
    }
    lateinit var knownPosition: Position
    lateinit var physicalPosition: Position
    val liftedPieces = HashSet<UByte>()


    private fun generateMove() {
        val disappearances = knownPosition.occupiedSquares.minus(physicalPosition.occupiedSquares)
        val appearances = physicalPosition.occupiedSquares.minus(knownPosition.occupiedSquares)
        val temporarilyLiftedPieces = liftedPieces.minus(physicalPosition.occupiedSquares)

        if (disappearances.size == 1 && appearances.size == 1) {
            // normal move
        } else if (disappearances.size == 1 && appearances.isEmpty() && temporarilyLiftedPieces.isNotEmpty()) {
            // capture
        } else if (disappearances.size == 2 && appearances.size == 1) {
            // en passant
        } else if (disappearances.size == 2 && appearances.size == 2) {
            // castling
        }
    }

    override fun onNewPosition(position: Position) {
        handler.removeCallbacks(runnable)
        physicalPosition = position
        if (position == Position.STARTING_POSITION) {
            knownPosition = position
            // reset chessboard
            gameEventHandler.onGameStart()
            liftedPieces.clear()
        } else {
            liftedPieces.addAll(knownPosition.occupiedSquares.minus(physicalPosition.occupiedSquares))
            if (MOVE_DELAY > 0) handler.postDelayed(runnable, MOVE_DELAY)
            else runnable.run()
        }
    }

    override fun close() {
        connection.close()
    }

    companion object {
        val MOVE_DELAY = 25L // in ms
    }
}

@ExperimentalUnsignedTypes
private class Connection(activity: Activity, var newPositionHandler: NewPositionHandler?) :
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
            for (i in 0..8)
                positionBytes.add(data.poll()!!)
            newPositionHandler?.onNewPosition(Position(positionBytes))
        }
    }

    override fun onRunError(e: java.lang.Exception) {
        Log.e(LOG_TAG, "onRunError")
        throw e
    }

    companion object {
        val LOG_TAG = Connection::class.simpleName
        val prober = UsbSerialProber(ProbeTable().addProduct(0x1b4f, 0x9206, CdcAcmSerialDriver::class.java))
    }

}

@ExperimentalUnsignedTypes
internal class Position(_bytes: Collection<UByte>) {
    val occupiedSquares: Set<UByte>
    val bytes: List<UByte>

    init {
        if (_bytes.size != 8)
            throw LiBoardInvalidPositionError("Position was ${_bytes.size} bytes, expected 8")

        bytes = _bytes.toList()

        val s = HashSet<UByte>()
        _bytes.forEachIndexed { file, byte ->
            for (rank in 0 until 8) {
                if ((byte and (1u shl rank).toUByte()).toInt() != 0) {
                    s.add((8 * rank + (7 - file)).toUByte())
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

    internal class LiBoardInvalidPositionError(str: String) : Exception(str)
}

@ExperimentalUnsignedTypes
private interface NewPositionHandler {
    fun onNewPosition(position: Position)
}

internal interface GameEventHandler {
    fun onGameStart()
    fun onMove()
}

internal open class ConnectionException(str: String) : Exception(str)
internal class MissingDriverException(str: String) : ConnectionException(str)
internal class UsbPermissionException(str: String) : ConnectionException(str)