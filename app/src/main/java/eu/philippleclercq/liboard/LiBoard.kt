package eu.philippleclercq.liboard

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.Closeable
import java.util.*
import kotlin.collections.HashSet

@ExperimentalUnsignedTypes
internal class LiBoard(activity: Activity, gameEventHandler: GameEventHandler) : NewPositionHandler {
    private val connection = Connection(activity, this)

    override fun onNewPosition(position: LiBoardPosition) {
        TODO("Not yet implemented")
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
        if (availableDrivers.isEmpty()) throw LiBoardMissingDriverException("No drivers available")

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
            throw LiBoardUsbPermissionException("No Usb permission")
        }

        port = driver.ports.first()
        port.open(connection)
        port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        port.dtr = true
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
            newPositionHandler?.onNewPosition(LiBoardPosition(positionBytes))
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
internal class LiBoardPosition(_bytes: Collection<UByte>) {
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


    internal class LiBoardInvalidPositionError(str: String) : Exception(str)
}

@ExperimentalUnsignedTypes
private interface NewPositionHandler {
    fun onNewPosition(position: LiBoardPosition)
}

internal interface GameEventHandler {
    fun onGameStart()
    fun onMove()
}

internal open class LiBoardConnectionException(str: String) : Exception(str)
internal class LiBoardMissingDriverException(str: String) : LiBoardConnectionException(str)
internal class LiBoardUsbPermissionException(str: String) : LiBoardConnectionException(str)