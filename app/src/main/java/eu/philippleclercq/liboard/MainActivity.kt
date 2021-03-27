package eu.philippleclercq.liboard

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hoho.android.usbserial.driver.UsbSerialPort
import android.hardware.usb.UsbDeviceConnection

import com.hoho.android.usbserial.driver.UsbSerialDriver

import com.hoho.android.usbserial.driver.UsbSerialProber

import android.hardware.usb.UsbManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val port = serialConnect()
        textbox.text = port.toString()
    }

    fun serialConnect(): UsbSerialPort? {
        // Find all available drivers from attached devices.
        // Find all available drivers from attached devices.
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        Log.d("serialConnect", manager.toString())
        Log.d("serialConnect", UsbSerialProber.getDefaultProber().toString())
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        Log.d("serialConnect", availableDrivers.toString())
        if (availableDrivers.isEmpty()) {
            Log.d("serialConnect", "empty")
            return null
        }

        // Open a connection to the first available driver.

        // Open a connection to the first available driver.
        val driver = availableDrivers[0]
        val connection = manager.openDevice(driver.device) ?: return null
        Log.d("serialConnect", connection.toString())

        val port = driver.ports.first()
        Log.d("serialConnect", port.toString())

        port.open(connection)
        port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        return port
    }
}