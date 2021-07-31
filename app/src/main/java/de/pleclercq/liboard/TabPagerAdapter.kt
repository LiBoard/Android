package de.pleclercq.liboard

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.pleclercq.liboard.databinding.ItemTextBinding
import de.pleclercq.liboard.liboard.Game
import de.pleclercq.liboard.liboard.LiBoard
import de.pleclercq.liboard.liboard.toPgn
import de.pleclercq.liboard.util.ViewHolder

@ExperimentalUnsignedTypes
class TabPagerAdapter(private val liBoard: LiBoard) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var data: Array<String>

    init {
        updateData()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTextBinding.inflate(inflater, parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tb = (holder as ViewHolder).textbox
        tb.text = data[position]
        if (position == 1) {
            tb.textSize = 12F
            tb.textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
        } else {
            tb.textSize = 30F
            tb.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }
    }

    override fun getItemCount() = data.size

    fun updateData() {
        data = arrayOf(
            liBoard.board.toString(),
            Game(liBoard).toPgn(),
            liBoard.physicalPosition.toString()
        )
    }
}