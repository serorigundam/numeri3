package net.ketc.numeri.presentation.view.component.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.cache.Cacheable
import net.ketc.numeri.presentation.view.component.FooterViewHolder
import net.ketc.numeri.presentation.view.component.ReadableMore
import net.ketc.numeri.presentation.view.component.TweetViewHolder
import net.ketc.numeri.util.rx.AutoDisposable
import java.util.*

class TwitterRecyclerAdapter<T : Cacheable<Long>>(private val autoDisposable: AutoDisposable,
                                                  private val create: () -> TwitterViewHolder<T>,
                                                  private val readableMore: ReadableMore<MutableList<T>>? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val itemList = ArrayList<T>()
    val last: T?
        get() = itemList.lastOrNull()
    val first: T?
        get() = itemList.firstOrNull()

    private var viewAddition = 0

    var isReadMoreEnable: Boolean = false
        get() = field
        set(value) {
            field = value
            if (value) {
                notifyItemInserted(itemCount)
                viewAddition++
            } else {
                notifyItemRemoved(itemCount)
                viewAddition--
            }
        }

    override fun getItemViewType(position: Int): Int {
        return if (itemList.lastIndex >= position) RecyclerView.INVALID_TYPE
        else TYPE_FOOTER
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //ダサい…
        if (itemList.lastIndex >= position)
            when (holder) {
                is TweetViewHolder -> {
                    holder.bind(itemList[position] as Tweet)
                }
                else -> throw InternalError()
            }
    }

    override fun getItemCount() = itemList.size + viewAddition

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_FOOTER -> FooterViewHolder(readableMore ?: throw IllegalStateException("readableMore is not initialized"),
                    autoDisposable, parent.context)
            else -> create()
        }
    }

    fun insertTop(t: T) {
        itemList.add(0, t)
        notifyItemInserted(0)
    }

    fun insertAllToTop(list: List<T>) {
        itemList.addAll(0, list)
        notifyItemRangeInserted(0, list.size)
    }

    fun addAll(list: List<T>) {
        itemList.addAll(list)
        notifyItemRangeInserted(itemList.lastIndex, list.size)
    }

    fun remove(t: T) {
        val index = itemList.indexOf(t)
        if (index != -1) {
            itemList.remove(t)
            notifyItemRemoved(index)
        }
    }

    fun remove(id: Long) {
        itemList.firstOrNull { it.id == id }?.let { remove(it) }
    }

    companion object {
        val TYPE_FOOTER = 300
    }
}

abstract class TwitterViewHolder<in T : Cacheable<Long>>(view: View, protected val onClick: (T) -> Unit) : RecyclerView.ViewHolder(view) {
    abstract protected val autoDisposable: AutoDisposable
    abstract fun bind(cacheable: T)
}

