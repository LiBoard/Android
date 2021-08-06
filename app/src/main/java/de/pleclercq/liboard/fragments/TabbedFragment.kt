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
import de.pleclercq.liboard.MainActivity
import de.pleclercq.liboard.R
import de.pleclercq.liboard.TabPagerAdapter
import de.pleclercq.liboard.databinding.FragmentTabbedBinding
import de.pleclercq.liboard.liboard.*
import de.pleclercq.liboard.liboard.LiBoardEvent.Companion.TYPE_CONNECT
import de.pleclercq.liboard.liboard.LiBoardEvent.Companion.TYPE_DISCONNECT
import de.pleclercq.liboard.util.CreatePgnDocument
import de.pleclercq.liboard.util.UsbPermissionReceiver
import java.io.FileOutputStream

@ExperimentalUnsignedTypes
class TabbedFragment(private val activity: MainActivity) : Fragment(), LiBoardEventHandler {
	private val creditsFragment = CreditsFragment(activity)
	private lateinit var binding: FragmentTabbedBinding
	private val liBoard = LiBoard(activity, this)
	private val createDocument = registerForActivityResult(CreatePgnDocument()) { saveGame(it) }
	private val usbPermissionReceiver = UsbPermissionReceiver { attemptConnect() }
	private lateinit var adapter: TabPagerAdapter

	//region Lifecycle
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
		adapter = TabPagerAdapter(activity, liBoard)
		vp.adapter = adapter
		TabLayoutMediator(tl, vp) { tab, i -> tab.text = adapter.getTitle(i) }.attach()

		return binding.root
	}

	override fun onDestroy() {
		liBoard.disconnect()
		activity.unregisterReceiver(usbPermissionReceiver)
		super.onDestroy()
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.fragment_tabbed, menu)
		super.onCreateOptionsMenu(menu, inflater)
	}
	//endregion

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.export_game -> createDocument.launch("unnamed.pgn")
			R.id.credits -> activity.supportFragmentManager.beginTransaction()
				.replace(R.id.main_fragment_container_view, creditsFragment).addToBackStack("credits").commit()
			else -> return false
		}
		return true
	}

	override fun onEvent(e: LiBoardEvent) {
		activity.runOnUiThread {
			when (e.type) {
				TYPE_CONNECT -> binding.connectFab.hide()
				TYPE_DISCONNECT -> {
					binding.connectFab.show()
					Toast.makeText(activity, "LiBoard disconnected", Toast.LENGTH_SHORT).show()
				}
				else -> adapter.onEvent(e)
			}
		}
	}

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

	private fun gameString() = Game(liBoard).toPgn()
}