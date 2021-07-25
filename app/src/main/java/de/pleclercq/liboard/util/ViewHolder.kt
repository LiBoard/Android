package de.pleclercq.liboard.util

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.pleclercq.liboard.databinding.ItemTextBinding

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val textbox: TextView = ItemTextBinding.bind(view).textbox
}