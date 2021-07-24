package de.pleclercq.liboard.fragments

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.pleclercq.liboard.MainActivity
import de.pleclercq.liboard.R
import de.pleclercq.liboard.databinding.FragmentCreditsBinding

@ExperimentalUnsignedTypes
internal class CreditsFragment(private val activity: MainActivity) : Fragment() {
    private lateinit var binding: FragmentCreditsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreditsBinding.inflate(inflater, container, false)
        binding.closeCredits.setOnClickListener { activity.supportFragmentManager.popBackStack() }
        binding.creditsTextView.loadData(
            Base64.encodeToString(getString(R.string.credits_html).toByteArray(), Base64.NO_PADDING),
            "text/html",
            "base64"
        )
        return binding.root
    }
}