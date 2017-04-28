package net.ketc.numeri.presentation.view.component.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import net.ketc.numeri.domain.entity.TweetsDisplay
import net.ketc.numeri.presentation.view.component.EmptyViewHolder
import net.ketc.numeri.presentation.view.component.SimpleTextViewHolder
import net.ketc.numeri.util.android.getResourceId
import java.util.*

class TweetsDisplayRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = ArrayList<TweetsDisplay>()
    val displayItemCount: Int
        get() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is SimpleTextViewHolder) {
            holder.bind(items[position].name)
        }
    }

    override fun getItemCount(): Int = items.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val ctx = parent.context
        return when (viewType) {
            DISPLAY -> SimpleTextViewHolder(ctx).apply {
                itemView.background = ctx.getDrawable(ctx.getResourceId(android.R.attr.selectableItemBackground))
                itemView.isClickable = true
            }
            EMPTY -> EmptyViewHolder(ctx)
            else -> throw InternalError()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items.lastIndex >= position) {
            DISPLAY
        } else {
            EMPTY
        }
    }

    fun add(display: TweetsDisplay) {
        items.add(display)
        notifyItemInserted(items.lastIndex)
    }

    fun remove(display: TweetsDisplay) {
        val removedIndex = items.indexOfFirst { it.id == display.id }
        if (removedIndex != -1) {
            items.removeIf { it.id == display.id }
            notifyItemRemoved(removedIndex)
        }
    }

    fun replace(to: TweetsDisplay, by: TweetsDisplay) {
        val toIndex = items.indexOfFirst { to.id == it.id }
        val byIndex = items.indexOfFirst { by.id == it.id }
        val temp = items[toIndex]
        items[toIndex] = items[byIndex]
        items[byIndex] = temp
        notifyItemMoved(byIndex, toIndex)
    }

    fun get(position: Int) = items[position]


    companion object {
        private val EMPTY = 100
        private val DISPLAY = 200
    }

}