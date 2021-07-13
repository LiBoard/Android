/*  Copyright (C) 2021  Philipp Leclercq

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version. */

package de.pleclercq.liboard

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), LiBoard.EventHandler {
    private val liBoard = LiBoard(this, this)

    //region Activity lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        attemptConnect()
    }

    override fun onDestroy() {
        liBoard.disconnect()
        super.onDestroy()
    }
    //endregion

    //region LiBoard.EventHandler
    override fun onGameStart() {
        runOnUiThread { textbox.text = liBoard.board.toString() }
    }

    override fun onMove() {
        runOnUiThread { textbox.text = liBoard.board.toString() }
    }

    override fun onConnect() {
        runOnUiThread {
            button.text = getString(R.string.disconnect)
            Toast.makeText(this, "LiBoard connected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDisconnect() {
        runOnUiThread {
            button.text = getString(R.string.connect)
            Toast.makeText(this, "LiBoard disconnected", Toast.LENGTH_SHORT).show()
        }
    }
    //endregion

    //region UI events
    @Suppress("UNUSED_PARAMETER")
    fun onButtonPress(view: View) = if (liBoard.isConnected) liBoard.disconnect() else attemptConnect()
    //endregion

    private fun attemptConnect() {
        try {
            liBoard.connect()
        } catch (e: LiBoard.MissingDriverException) {
            Toast.makeText(this, "No Board connected", Toast.LENGTH_SHORT).show()
        } catch (e: LiBoard.UsbPermissionException) {
            Toast.makeText(this, "No USB Permission", Toast.LENGTH_SHORT).show()
        }
    }
}