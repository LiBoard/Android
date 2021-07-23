package de.pleclercq.liboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.pleclercq.liboard.databinding.FragmentGameBinding

@ExperimentalUnsignedTypes
class GameFragment(private val activity: MainActivity) : Fragment() {
    internal lateinit var binding: FragmentGameBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGameBinding.inflate(inflater, container, false)
        binding.connectFab.setOnClickListener { activity.attemptConnect() }
        return binding.root
    }
}