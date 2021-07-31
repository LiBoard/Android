package de.pleclercq.liboard.fragments

import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import de.pleclercq.liboard.*
import de.pleclercq.liboard.databinding.FragmentTabbedBinding
import de.pleclercq.liboard.liboard.*
import de.pleclercq.liboard.liboard.Connection
import de.pleclercq.liboard.util.CreatePgnDocument
import de.pleclercq.liboard.util.UsbPermissionReceiver
import java.io.FileOutputStream

@ExperimentalUnsignedTypes
class TabbedFragment(private val activity: MainActivity) : Fragment(), LiBoardEventHandler {
    private lateinit var binding: FragmentTabbedBinding
    private val liBoard = LiBoard(activity, this)
    private val createDocument = registerForActivityResult(CreatePgnDocument()) { saveGame(it) }
    private val usbPermissionReceiver = UsbPermissionReceiver { attemptConnect() }
    private lateinit var adapter: TabPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity.registerReceiver(usbPermissionReceiver, IntentFilter(UsbPermissionReceiver.ACTION))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabbedBinding.inflate(inflater, container, false)

        if (liBoard.isConnected) binding.connectFab.hide()
        binding.connectFab.setOnClickListener { attemptConnect() }

        val tl = binding.tabLayout
        val vp = binding.viewPager
        adapter = TabPagerAdapter(liBoard)
        vp.adapter = adapter
        TabLayoutMediator(tl, vp) { tab, i ->
            tab.text = when (i) {
                0 -> "Board"
                1 -> "Moves"
                2 -> "Diagnostics"
                else -> "Not found"
            }
        }.attach()

        return binding.root
    }

    override fun onDestroy() {
        liBoard.disconnect()
        activity.unregisterReceiver(usbPermissionReceiver)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_game, menu)
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
        activity.runOnUiThread { update() }
    }

    override fun onMove() {
        activity.runOnUiThread { update() }
    }

    override fun onNewPhysicalPosition() {
        activity.runOnUiThread { update() }
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

    /**
     * Attempts to connect to the physical board.
     */
    private fun attemptConnect() {
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
    private fun saveGame(uri: Uri) {
        try {
            activity.contentResolver.openFileDescriptor(uri, "w")?.use { pfd: ParcelFileDescriptor ->
                FileOutputStream(pfd.fileDescriptor).use { fos: FileOutputStream ->
                    fos.write(gameString().toByteArray())
                }
            }
        } catch (e: Exception) {
            Log.w("exportGame", e)
            Toast.makeText(activity, "An error occurred while exporting", Toast.LENGTH_SHORT).show()
        }
    }

    private fun update() {
        adapter.updateData()
        adapter.notifyDataSetChanged()
    }

    private fun gameString() = Game(liBoard).toPgn()
}