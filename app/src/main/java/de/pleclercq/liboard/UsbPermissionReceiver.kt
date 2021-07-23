package de.pleclercq.liboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager

@ExperimentalUnsignedTypes
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