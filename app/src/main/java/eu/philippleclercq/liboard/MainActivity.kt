package eu.philippleclercq.liboard

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity(), LiBoard.EventHandler {
    private val liBoard = LiBoard(this, this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            liBoard.connect()
        } catch (e: LiBoard.MissingDriverException) {
            Toast.makeText(this, "No Board connected", Toast.LENGTH_SHORT).show()
        } catch (e: LiBoard.UsbPermissionException) {
            Toast.makeText(this, "No USB Permission", Toast.LENGTH_SHORT).show()
        }
        button.text = if (liBoard.isConnected) "Disconnect" else "Connect"
    }

    override fun onDestroy() {
        liBoard.disconnect()
        super.onDestroy()
    }

    override fun onGameStart() {
        runOnUiThread { textbox.text = liBoard.board.toString() }
    }

    override fun onMove() {
        runOnUiThread { textbox.text = liBoard.board.toString() }
    }

    override fun onDisconnect() {
        runOnUiThread {
            button.text = "Connect"
            Toast.makeText(this, "LiBoard disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnect() {
        runOnUiThread {
            button.text = "Disconnect"
            Toast.makeText(this, "LiBoard connected", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onButtonPress(view: View) = if (liBoard.isConnected) liBoard.disconnect() else liBoard.connect()
}