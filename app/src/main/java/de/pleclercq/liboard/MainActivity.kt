/*  Copyright (C) 2021  Philipp Leclercq

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version. */

package de.pleclercq.liboard

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import de.pleclercq.liboard.databinding.ActivityMainBinding
import java.io.FileOutputStream


@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), LiBoard.EventHandler {
    private val liBoard = LiBoard(this, this)
    private lateinit var binding: ActivityMainBinding
    private val usbPermissionReceiver = UsbPermissionReceiver()
    private val createDocument = registerForActivityResult(MyCreateDocument()) { saveGame(it) }

    //region Activity lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.connectFab.setOnClickListener { attemptConnect() }
        setContentView(binding.root)
        registerReceiver(usbPermissionReceiver, IntentFilter(UsbPermissionReceiver.ACTION))
        attemptConnect()
    }

    override fun onDestroy() {
        unregisterReceiver(usbPermissionReceiver)
        liBoard.disconnect()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_bar, menu)
        return true
    }
    //endregion

    //region LiBoard
    override fun onGameStart() {
        runOnUiThread { binding.textbox.text = liBoard.board.toString() }
    }

    override fun onMove() {
        runOnUiThread { binding.textbox.text = liBoard.board.toString() }
    }

    override fun onNewPhysicalPosition() {
        Log.d("onNewPhysicalPosition", "${liBoard.physicalPosition}")
    }

    override fun onConnect() {
        runOnUiThread {
            binding.connectFab.hide()
        }
    }

    override fun onDisconnect() {
        runOnUiThread {
            binding.connectFab.show()
            Toast.makeText(this, "LiBoard disconnected", Toast.LENGTH_SHORT).show()
        }
    }
    //endregion

    //region UI events
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.export_game -> createDocument.launch("application/x-chess-pgn")
            //TODO implement credits
            R.id.credits -> Toast.makeText(this, "Credits are not yet implemented.", Toast.LENGTH_SHORT).show()
            else -> return false
        }
        return true
    }

    /**
     * Exports a game by sending it as an [Intent].
     */
    private fun saveGame(uri: Uri) = try {
        contentResolver.openFileDescriptor(uri, "w")?.use { pfd: ParcelFileDescriptor ->
            FileOutputStream(pfd.fileDescriptor).use { fos: FileOutputStream ->
                fos.write(liBoard.exportGame().toPgn(true, true).toByteArray())
            }
        }
    } catch (e: Exception) {
        Log.w("exportGame", e)
        Toast.makeText(this, "An error occurred while exporting", Toast.LENGTH_SHORT).show()
    }
    //endregion

    private fun attemptConnect() {
        try {
            liBoard.connect()
        } catch (e: LiBoard.MissingDriverException) {
            Log.d("attemptConnect", e::class.simpleName!!)
            Toast.makeText(this, "No Board connected", Toast.LENGTH_SHORT).show()
        } catch (e: LiBoard.UsbPermissionException) {
            Log.d("attemptConnect", e::class.simpleName!!)
        }
    }

    internal class UsbPermissionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context is MainActivity && intent != null && intent.action == ACTION
                && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            )
                context.attemptConnect()
        }

        companion object {
            const val ACTION = "de.pleclercq.liboard.USB_PERMISSION_GRANTED"
        }
    }

    private class MyCreateDocument : ActivityResultContract<String, Uri>() {
        override fun createIntent(context: Context, input: String?) = Intent().apply {
            action = Intent.ACTION_CREATE_DOCUMENT
            type = input
        }

        override fun parseResult(resultCode: Int, intent: Intent?) =
            if (intent == null || resultCode != Activity.RESULT_OK) null
            else intent.data
    }
}
