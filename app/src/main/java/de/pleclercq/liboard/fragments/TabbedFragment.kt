package de.pleclercq.liboard.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import de.pleclercq.liboard.MainActivity
import de.pleclercq.liboard.databinding.FragmentTabbedBinding

@ExperimentalUnsignedTypes
internal class TabbedFragment(private val activity: MainActivity) : Fragment(), TabLayout.OnTabSelectedListener {
    private lateinit var binding: FragmentTabbedBinding
    private val boardFragment = BoardFragment(activity)

    //region Lifecycle
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabbedBinding.inflate(inflater, container, false)
        val tl = binding.tabLayout
        tl.addOnTabSelectedListener(this)
        for (tab in Tab.values())
            tl.addTab(tl.newTab().apply {
                text = tab.title
                tag = tab
            })
        return binding.root
    }
    //endregion

    //region OnTabSelectedListener
    override fun onTabSelected(tab: TabLayout.Tab) {
        when (tab.tag) {
            Tab.BOARD -> activity.supportFragmentManager.beginTransaction()
                .replace(binding.contentHolder.id, boardFragment).commit()
            else -> Log.w("Unknown tab selected", "${tab.tag}")
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        Log.d("Tab unselected", "${tab.tag}")
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        Log.d("Tab reselected", "${tab.tag}")
    }
    //endregion

    private enum class Tab(val title: String) {
        BOARD("Board"),
        MOVES("Moves"),
        DIAGNOSTICS("Diagnostics")
    }
}