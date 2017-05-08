package net.ketc.numeri.presentation.view.component.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import net.ketc.numeri.domain.entity.TweetsDisplayGroup
import net.ketc.numeri.presentation.view.component.SimpleTextViewHolder
import net.ketc.numeri.presentation.view.component.ui.simple.SimpleTextItemUI
import net.ketc.numeri.util.android.getResourceId
import java.util.*

class TweetsDisplayGroupsRecyclerAdapter(private val onClick: (TweetsDisplayGroup) -> Unit) : RecyclerView.Adapter<SimpleTextViewHolder>() {

    private val items = ArrayList<TweetsDisplayGroup>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SimpleTextViewHolder(SimpleTextItemUI(parent.context)).apply {
        val ctx = parent.context
        itemView.background = ctx.getDrawable(ctx.getResourceId(android.R.attr.selectableItemBackground))
    }

    override fun onBindViewHolder(holder: SimpleTextViewHolder, position: Int) {
        holder.bind(items[position].name)
        holder.itemView.setOnClickListener { onClick(items[position]) }
    }

    override fun getItemCount() = items.size

    fun add(group: TweetsDisplayGroup) {
        items.add(group)
        notifyItemInserted(items.lastIndex)
    }

    fun remove(group: TweetsDisplayGroup) {
        val removedIndex = items.indexOf(group)
        if (removedIndex != -1) {
            items.removeAt(removedIndex)
            notifyItemRemoved(removedIndex)
        }
    }

    fun get(position: Int): TweetsDisplayGroup = items[position]
}
