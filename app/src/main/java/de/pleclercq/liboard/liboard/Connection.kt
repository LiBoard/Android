/*
 * LiBoard
 * Copyright (C) 2021 Philipp Leclercq
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.pleclercq.liboard.liboard

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import de.pleclercq.liboard.util.UsbPermissionReceiver
import java.io.Closeable
import java.util.*
import java.util.concurrent.Executors

/**
 * Manages the Serial connection to the board.
 * Connects to the board when created.
 * Handles errors and incoming data.
 * Closes the serial port when it's closed.
 */
@ExperimentalUnsignedTypes
internal class Connection(context: Context, val liboard: LiBoard) :
	Closeable, SerialInputOutputManager.Listener {
	private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
	private val port: UsbSerialPort
	private val data = LinkedList<UByte>()

	init {
		val availableDrivers = prober.findAllDrivers(usbManager)
		Log.d("serialConnect", "Drivers: $availableDrivers")
		if (availableDrivers.isEmpty()) throw MissingDriverException("No drivers available")

		// Open a connection to the first available driver.
		val driver = availableDrivers.first()
		if (!usbManager.hasPermission(driver.device)) {
			usbManager.requestPermission(
				driver.device,
				PendingIntent.getBroadcast(
					context,
					0,
					Intent().apply { action = UsbPermissionReceiver.ACTION },
					0
				)
			)
			throw UsbPermissionException("No Usb permission")
		} else {
			val connection = usbManager.openDevice(driver.device)
			port = driver.ports.first()
			port.open(connection)
			port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
			port.dtr = true

			Executors.newSingleThreadExecutor().submit(SerialInputOutputManager(port, this))
		}
	}

	/**
	 * Close the connection
	 */
	override fun close() {
		port.close()
	}

	/**
	 * Handles incoming data.
	 * Calls [LiBoard.onNewPhysicalPosition] when enough data (8 bytes) came in.
	 */
	override fun onNewData(_data: ByteArray) {
		Log.v(LOG_TAG, "New data: $_data")
		data.addAll(_data.toUByteArray())
		if (data.size >= 8) {
			var bitboard = 0UL
			for (i in 7 downTo 0)
				bitboard = bitboard or (data.poll()!!.toULong() shl (8 * i))
			liboard.onNewPhysicalPosition(PhysicalPosition(bitboard))
		}
	}

	/**
	 * Gets called when an error regarding the connection is encountered.
	 * Disconnects the board.
	 */
	override fun onRunError(e: java.lang.Exception) {
		Log.e(LOG_TAG, "onRunError", e)
		liboard.disconnect()
	}

	companion object {
		val LOG_TAG = Connection::class.simpleName
		val prober = UsbSerialProber(ProbeTable().addProduct(0x1b4f, 0x9206, CdcAcmSerialDriver::class.java))
	}

	internal abstract class ConnectionException(str: String) : Exception(str)
	internal class MissingDriverException(str: String) : ConnectionException(str)
	internal class UsbPermissionException(str: String) : ConnectionException(str)
}