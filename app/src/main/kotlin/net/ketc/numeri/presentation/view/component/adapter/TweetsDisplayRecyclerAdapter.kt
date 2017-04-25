package net.ketc.numeri.presentation.view.component.adapter

import android.R
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import net.ketc.numeri.domain.entity.TweetsDisplay
import net.ketc.numeri.presentation.view.component.EmptyViewHolder
import net.ketc.numeri.presentation.view.component.SimpleTextViewHolder
import net.ketc.numeri.util.android.getResourceId
import java.util.*

class TweetsDisplayRecyclerAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = ArrayList<Pair<TweetsDisplay, String>>()
    val displayItemCount: Int
        get() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is SimpleTextViewHolder) {
            holder.bind(items[position].second)
        }
    }

    override fun getItemCount(): Int = items.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val ctx = parent.context
        return when (viewType) {
            DISPLAY -> SimpleTextViewHolder(ctx).apply {
                itemView.background = ctx.getDrawable(ctx.getResourceId(R.attr.selectableItemBackground))
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

    fun add(pair: Pair<TweetsDisplay, String>) {
        items.add(pair)
        notifyItemInserted(items.lastIndex)
    }

    fun remove(display: TweetsDisplay) {
        val removedIndex = items.indexOfFirst { it.first.id == display.id }
        if (removedIndex != -1) {
            items.removeIf { it.first.id == display.id }
            notifyItemRemoved(removedIndex)
        }
    }

    fun replace(to: TweetsDisplay, by: TweetsDisplay) {
        val toIndex = items.indexOfFirst { to.id == it.first.id }
        val byIndex = items.indexOfFirst { by.id == it.first.id }
        val temp = items[toIndex]
        items[toIndex] = items[byIndex]
        items[byIndex] = temp
        notifyItemMoved(byIndex, toIndex)
    }

    fun get(position: Int) = items[position].first


    companion object {
        private val EMPTY = 100
        private val DISPLAY = 200
    }

}