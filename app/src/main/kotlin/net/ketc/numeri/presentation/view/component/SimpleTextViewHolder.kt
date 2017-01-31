package net.ketc.numeri.presentation.view.component

import android.content.Context
import android.support.v7.widget.RecyclerView
import net.ketc.numeri.presentation.view.component.ui.simple.SimpleTextItemUI
import net.ketc.numeri.presentation.view.component.ui.simple.text

class SimpleTextViewHolder(ctx: Context) : RecyclerView.ViewHolder(SimpleTextItemUI(ctx).createView()) {
    fun bind(text: String) {
        itemView.text.text = text
    }
}
