/*  Copyright (C) 2021  Philipp Leclercq

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version. */

package de.pleclercq.liboard

import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.pleclercq.liboard.databinding.ActivityMainBinding


@ExperimentalUnsignedTypes
class MainActivity : AppCompatActivity() {
    private val gameFragment = GameFragment(this)
    private lateinit var binding: ActivityMainBinding
    private val usbPermissionReceiver = UsbPermissionReceiver { gameFragment.attemptConnect() }

    //region Activity lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().add(binding.mainFragmentContainerView.id, gameFragment).commit()
        registerReceiver(usbPermissionReceiver, IntentFilter(UsbPermissionReceiver.ACTION))
    }

    override fun onDestroy() {
        unregisterReceiver(usbPermissionReceiver)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }
    //endregion

    //region UI events
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //TODO implement credits
            R.id.credits -> Toast.makeText(this, "Credits are not yet implemented.", Toast.LENGTH_SHORT).show()
            else -> return false
        }
        return true
    }
    //endregion
}
