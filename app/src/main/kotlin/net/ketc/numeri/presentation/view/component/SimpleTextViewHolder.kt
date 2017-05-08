package net.ketc.numeri.presentation.view.component

import android.support.v7.widget.RecyclerView
import net.ketc.numeri.presentation.view.component.ui.simple.ISimpleTextItemUI
import net.ketc.numeri.presentation.view.component.ui.simple.SimpleTextItemUI

class SimpleTextViewHolder(ui: SimpleTextItemUI) : RecyclerView.ViewHolder(ui.createView()), ISimpleTextItemUI by ui {
    fun bind(text: String) {
        this.text.text = text
    }
}
