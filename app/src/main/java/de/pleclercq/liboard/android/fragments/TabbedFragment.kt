/*
 * LiBoard
 * Copyright (C) 2021 Philipp Leclercq
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package de.pleclercq.liboard.android.fragments

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import de.pleclercq.liboard.R
import de.pleclercq.liboard.android.adapters.TabPagerAdapter
import de.pleclercq.liboard.android.util.CreatePgnDocument
import de.pleclercq.liboard.android.util.UsbPermissionReceiver
import de.pleclercq.liboard.databinding.FragmentTabbedBinding
import de.pleclercq.liboard.liboard.*
import de.pleclercq.liboard.liboard.Event.CONNECT
import de.pleclercq.liboard.liboard.Event.DISCONNECT
import java.io.FileOutputStream

@Suppress("unused")
@ExperimentalUnsignedTypes
class TabbedFragment : Fragment(), EventHandler {
	private lateinit var binding: FragmentTabbedBinding
	private val createDocument = registerForActivityResult(CreatePgnDocument()) { saveGame(it) }
	private val usbPermissionReceiver = UsbPermissionReceiver { attemptConnect() }
	private lateinit var adapter: TabPagerAdapter

	//region Lifecycle
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		LiBoard.eventHandlers.add(this)
		setHasOptionsMenu(true)
		requireContext().registerReceiver(usbPermissionReceiver, IntentFilter(UsbPermissionReceiver.ACTION))
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentTabbedBinding.inflate(inflater, container, false)

		if (LiBoard.isConnected) binding.connectFab.hide()
		binding.connectFab.setOnClickListener { attemptConnect() }

		val tl = binding.tabLayout
		val vp = binding.viewPager
		adapter = TabPagerAdapter(requireContext())
		vp.adapter = adapter
		TabLayoutMediator(tl, vp) { tab, i -> tab.text = adapter.getTitle(i) }.attach()

		return binding.root
	}

	override fun onDestroy() {
		LiBoard.disconnect()
		LiBoard.eventHandlers.remove(this)
		requireContext().unregisterReceiver(usbPermissionReceiver)
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
			R.id.share_game -> {
				Intent().apply {
					action = ACTION_SEND
					putExtra(EXTRA_TEXT, gameString())
					type = "text/plain"
				}.let {
					Intent.createChooser(it, null)
				}.let { startActivity(it) }
			}
			R.id.takeback -> LiBoard.takeback()
			R.id.app_settings -> (requireContext() as AppCompatActivity).supportFragmentManager.beginTransaction()
				.replace(R.id.main_fragment_container_view, SettingsFragment()).addToBackStack("settings").commit()
			R.id.documentation -> {
				Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://liboard.github.io/doc/android")
				).let { startActivity(it) }
			}
			R.id.about -> (requireContext() as AppCompatActivity).supportFragmentManager.beginTransaction()
				.replace(R.id.main_fragment_container_view, AboutFragment()).addToBackStack("about").commit()
			else -> return false
		}
		return true
	}

	override fun onEvent(e: Event) {
		(requireContext() as Activity).runOnUiThread {
			when (e) {
				CONNECT -> binding.connectFab.hide()
				DISCONNECT -> {
					binding.connectFab.show()
					Toast.makeText(activity, "LiBoard disconnected", Toast.LENGTH_SHORT).show()
				}
				else -> adapter.clockManager.onEvent(e)
			}
		}
	}

	/**
	 * Attempts to connect to the physical board.
	 */
	private fun attemptConnect() {
		try {
			LiBoard.connect(requireContext())
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
			(requireContext() as Activity).contentResolver.openFileDescriptor(uri, "w")
				?.use { pfd: ParcelFileDescriptor ->
					FileOutputStream(pfd.fileDescriptor).use { fos: FileOutputStream ->
						fos.write(gameString().toByteArray())
					}
				}
		} catch (e: Exception) {
			Log.w("exportGame", e)
			Toast.makeText(activity, "An error occurred while exporting", Toast.LENGTH_SHORT).show()
		}
	}

	private fun gameString() = Game(LiBoard).toPgn()
}
