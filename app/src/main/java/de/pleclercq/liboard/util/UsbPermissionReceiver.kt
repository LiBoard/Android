package de.pleclercq.liboard.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager

@ExperimentalUnsignedTypes
internal class UsbPermissionReceiver(private val callback: Runnable) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.action == ACTION
            && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
        )
            callback.run()
    }

    companion object {
        const val ACTION = "de.pleclercq.liboard.USB_PERMISSION_GRANTED"
    }
}