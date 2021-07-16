/*  Copyright (C) 2021  Philipp Leclercq

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version. */

package de.pleclercq.liboard

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.BoardEvent
import com.github.bhlangonijr.chesslib.BoardEventListener
import com.github.bhlangonijr.chesslib.BoardEventType
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.Closeable
import java.util.*
import java.util.concurrent.Executors

/**
 * A class handling everything related to the board.
 * Handles the serial connection, incoming data and move validation.
 *
 * @param activity The [Activity] this object belongs to. Required for access to system services, permissions etc.
 * @param eventHandler A [EventHandler] that is used to react to game starts, moves and connection related events.
 *
 * @property knownPosition The physical position matching [board].
 * @property physicalPosition The current physical position.
 * @property liftedPieces All pieces that were lifted (temporarily or permanently) since the last move.
 */
@ExperimentalUnsignedTypes
internal class LiBoard(private val activity: Activity, private var eventHandler: EventHandler) : BoardEventListener {
    lateinit var board: Board
        private set
    private var knownPosition = PhysicalPosition.STARTING_POSITION
    internal var physicalPosition = PhysicalPosition.STARTING_POSITION
        private set
    private val liftedPieces = HashSet<Int>()
    private var connection: Connection? = null
    val isConnected get() = connection != null

    init {
        newBoard()
    }

    //region Position
    /**
     * Tries to find a move that matches the difference between [knownPosition] and [physicalPosition].
     * Makes the move if one was found.
     *
     * @return whether a move was found
     */
    private fun generateMove(): Boolean {
        val disappearances = knownPosition.occupiedSquares.minus(physicalPosition.occupiedSquares)
        val appearances = physicalPosition.occupiedSquares.minus(knownPosition.occupiedSquares)
        val temporarilyLiftedPieces = liftedPieces.intersect(physicalPosition.occupiedSquares)

        if (disappearances.size == 1 && appearances.size == 1) {
            // normal move
            val m = board.findMove(disappearances.first(), appearances.first())
            return m != null && !board.isCastling(m) && !board.isCapture(m) && board.doMove(m)
        } else if (disappearances.size == 1 && appearances.isEmpty() && temporarilyLiftedPieces.isNotEmpty()) {
            // "normal" capture (not e.p.)
            for (to in temporarilyLiftedPieces) {
                val m = board.findMove(disappearances.first(), to)
                if (m != null && board.isNormalCapture(m) && board.doMove(m))
                    return true
            }
            return false
        } else if (disappearances.size == 2 && appearances.size == 1) {
            // en passant
            for (from in disappearances) {
                for (to in appearances) {
                    val m = board.findMove(from, to)
                    if (m != null && board.isEnPassant(m) && board.doMove(m))
                        return true
                }
            }
            return false
        } else if (disappearances.size == 2 && appearances.size == 2) {
            // castling
            for (from in disappearances) {
                for (to in appearances) {
                    val m = board.findMove(from, to)
                    if (m != null && board.isCastling(m) && board.doMove(m))
                        return true
                }
            }
            return false
        }
        return false
    }

    /**
     * Gets called when a new physical board position comes in.
     * Resets the board to the starting position if necessary
     * or tries to find a legal move matching the position otherwise.
     */
    private fun onNewPhysicalPosition(position: PhysicalPosition) {
        eventHandler.onNewPhysicalPosition()
        physicalPosition = position
        if (position == PhysicalPosition.STARTING_POSITION) {
            newBoard()
            updateKnownPosition()
            eventHandler.onGameStart()
        } else {
            liftedPieces.addAll(knownPosition.occupiedSquares.minus(this.physicalPosition.occupiedSquares))
            generateMove()
        }
    }

    /**
     * Gets called when a move happens.
     *  Calls [updateKnownPosition] and the [eventHandler]'s [EventHandler.onMove] function.
     */
    override fun onEvent(event: BoardEvent) {
        updateKnownPosition()
        eventHandler.onMove()
    }

    /**
     * Creates a new [Board] and registers the [BoardEventListener].
     */
    private fun newBoard() {
        board = Board()
        board.addEventListener(BoardEventType.ON_MOVE, this)
    }

    /**
     * Sets [knownPosition] tp [physicalPosition] and clears [liftedPieces].
     */
    private fun updateKnownPosition() {
        knownPosition = physicalPosition
        liftedPieces.clear()
    }
    //endregion

    //region Connection
    /**
     * Manages the Serial connection to the board.
     * Connects to the board when created.
     * Handles errors and incoming data.
     * Closes the serial port when it's closed.
     */
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

        /**
         * Close the connection
         */
        override fun close() {
            port.close()
        }

        /**
         * Handles incoming data.
         * Calls [onNewPhysicalPosition] when enough data (8 bytes) came in.
         */
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
                liboard.onNewPhysicalPosition(PhysicalPosition(positionBytes))
            }
        }

        /**
         * Gets called when an error regarding the connection is encountered.
         * Disconnects the board.
         */
        override fun onRunError(e: java.lang.Exception) {
            Log.e(LOG_TAG, "onRunError")
            liboard.disconnect()
        }

        companion object {
            val LOG_TAG = Connection::class.simpleName
            val prober = UsbSerialProber(ProbeTable().addProduct(0x1b4f, 0x9206, CdcAcmSerialDriver::class.java))
        }

    }

    /**
     * Creates a new [Connection].
     */
    fun connect() {
        if (isConnected) disconnect()
        connection = Connection(activity, this)
        eventHandler.onConnect()
    }

    /**
     * Closes and unregisters the current [Connection].
     */
    fun disconnect() {
        connection?.close()
        connection = null
        eventHandler.onDisconnect()
    }
    //endregion

    //region Interfaces&Exceptions
    /**
     * Handler for game starts, moves and connection related events.
     */
    internal interface EventHandler {
        /**
         * Called when a new game starts.
         */
        fun onGameStart()

        /**
         * Called when a legal move is detected.
         */
        fun onMove()

        /**
         * Called when the physical board position changes.
         */
        fun onNewPhysicalPosition()

        /**
         * Called when a LiBoard is connected.
         */
        fun onConnect()

        /**
         * Called when the LiBoard is disconnected.
         */
        fun onDisconnect()
    }

    internal abstract class ConnectionException(str: String) : Exception(str)
    internal class MissingDriverException(str: String) : ConnectionException(str)
    internal class UsbPermissionException(str: String) : ConnectionException(str)
    internal class LiBoardInvalidPositionError(str: String) : Exception(str)
    //endregion
}