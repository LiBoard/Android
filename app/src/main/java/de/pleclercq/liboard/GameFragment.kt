package de.pleclercq.liboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import de.pleclercq.liboard.databinding.FragmentGameBinding
import java.io.FileOutputStream

@ExperimentalUnsignedTypes
internal class GameFragment(private val activity: MainActivity) : Fragment(),
    LiBoardEventHandler {
    private val liBoard = LiBoard(activity, this)
    private val createDocument = registerForActivityResult(CreatePgnDocument()) { saveGame(it) }
    private lateinit var binding: FragmentGameBinding

    //region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        if (liBoard.isConnected) binding.connectFab.hide()
        binding.connectFab.setOnClickListener { attemptConnect() }
        binding.textbox.text = liBoard.board.toString()
        return binding.root
    }

    override fun onDestroy() {
        liBoard.disconnect()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity.menuInflater.inflate(R.menu.fragment_game, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    //endregion

    //region UI events
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.export_game -> createDocument.launch("unnamed.pgn")
            else -> return false
        }
        return true
    }
    //endregion

    //region LiBoard
    override fun onGameStart() {
        activity.runOnUiThread { binding.textbox.text = liBoard.board.toString() }
    }

    override fun onMove() {
        activity.runOnUiThread { binding.textbox.text = liBoard.board.toString() }
    }

    override fun onNewPhysicalPosition() {
        Log.d("onNewPhysicalPosition", "${liBoard.physicalPosition}")
    }

    override fun onConnect() {
        activity.runOnUiThread {
            binding.connectFab.hide()
        }
    }

    override fun onDisconnect() {
        activity.runOnUiThread {
            binding.connectFab.show()
            Toast.makeText(activity, "LiBoard disconnected", Toast.LENGTH_SHORT).show()
        }
    }
    //endregion

    internal fun attemptConnect() {
        try {
            liBoard.connect()
        } catch (e: Connection.MissingDriverException) {
            Log.d("attemptConnect", e::class.simpleName!!)
            Toast.makeText(activity, "No Board connected", Toast.LENGTH_SHORT).show()
        } catch (e: Connection.UsbPermissionException) {
            Log.d("attemptConnect", e::class.simpleName!!)
        }
    }

    /**
     * Exports a game by sending it as an [Intent].
     */
    private fun saveGame(uri: Uri) = try {
        activity.contentResolver.openFileDescriptor(uri, "w")?.use { pfd: ParcelFileDescriptor ->
            FileOutputStream(pfd.fileDescriptor).use { fos: FileOutputStream ->
                fos.write(liBoard.exportGame().toPgn(true, true).toByteArray())
            }
        }
    } catch (e: Exception) {
        Log.w("exportGame", e)
        Toast.makeText(activity, "An error occurred while exporting", Toast.LENGTH_SHORT).show()
    }
}