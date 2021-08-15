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

import android.content.res.Configuration
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import de.pleclercq.liboard.BuildConfig
import de.pleclercq.liboard.databinding.FragmentWebviewBinding
import java.text.SimpleDateFormat
import java.util.*

class AboutFragment : Fragment() {
	val buildTime: String = Date(BuildConfig.VERSION_CODE * 1000L).let {
		SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).apply {
			timeZone = TimeZone.getTimeZone("UTC")
		}.format(it)
	}
	val html = """
	<center><h1>LiBoard</h1></center></br>
    <h3>Build time:</h3>BUILD_TIME<br/>
    <h3>Version name:</h3>VERSION_NAME<br/>
    <h3>Build type:</h3>BUILD_TYPE<br/>
	<h3>Credits</h3>
    <ul>
        <li><a href="https://github.com/bhlangonijr/chesslib">chesslib</a></li>
        <li><a href="https://github.com/mik3y/usb-serial-for-android">usb-serial-for-android</a></li>
        <li><a href="https://www.flaticon.com/authors/good-ware">Good Ware</a> for the icon</a></li>
    </ul>
	""".trimIndent()
		.replace("BUILD_TIME", buildTime)
		.replace("VERSION_NAME", BuildConfig.VERSION_NAME)
		.replace("BUILD_TYPE", BuildConfig.BUILD_TYPE)

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		val binding = FragmentWebviewBinding.inflate(inflater, container, false)
		binding.webView.loadData(
			Base64.encodeToString(html.toByteArray(), Base64.NO_PADDING),
			"text/html",
			"base64"
		)

		if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
			when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
				Configuration.UI_MODE_NIGHT_YES -> {
					WebSettingsCompat.setForceDark(binding.webView.settings, WebSettingsCompat.FORCE_DARK_ON)
				}
				Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
					WebSettingsCompat.setForceDark(binding.webView.settings, WebSettingsCompat.FORCE_DARK_OFF)
				}
			}
		}
		return binding.root
	}
}
