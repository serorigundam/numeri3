package net.ketc.numeri.presentation.view.component.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import net.ketc.numeri.domain.model.cache.Cacheable
import net.ketc.numeri.presentation.view.component.FooterViewHolder
import net.ketc.numeri.presentation.view.component.ReadableMore
import net.ketc.numeri.presentation.view.component.ui.UI
import net.ketc.numeri.presentation.view.component.ui.footer.EmptyViewUI
import net.ketc.numeri.presentation.view.component.ui.footer.FooterViewUI
import net.ketc.numeri.util.indexesIf
import net.ketc.numeri.util.rx.AutoDisposable
import kotlin.collections.ArrayList

class ReadableMoreRecyclerAdapter<T>(private val autoDisposable: AutoDisposable,
                                     private val create: () -> ReadableMoreViewHolder<T>,
                                     private val readableMore: ReadableMore<MutableList<T>>? = null) : RecyclerView.Adapter<ReadableMoreViewHolder<T>>() {

    private val itemList = ArrayList<T>()
    val last: T?
        get() = itemList.lastOrNull()
    val first: T?
        get() = itemList.firstOrNull()

    private var viewAddition = 0

    private val enabledAdditions = BooleanArray(2) { false }
    private var enabledTypeList: ArrayList<Int> = ArrayList()

    var isReadMoreEnabled: Boolean
        get() = enabledAdditions[TYPE_READ_MORE.second]
        set(value) {
            setAdditionEnabled(TYPE_READ_MORE, value)
        }

    var isEmptyFooterEnabled: Boolean
        get() = enabledAdditions[TYPE_EMPTY.second]
        set(value) {
            setAdditionEnabled(TYPE_EMPTY, value)
        }

    private fun setAdditionEnabled(pair: Pair<Int, Int>, enabled: Boolean) {
        val key = pair.second
        val upperEnabledCount = enabledAdditions.filterIndexed { i, _ -> key < i }.count { it }
        val changingPosition = viewAddition - upperEnabledCount + itemList.size
        val previous = enabledAdditions[key]
        enabledAdditions[key] = enabled
        val index = changingPosition - itemList.size
        if (previous != enabled) {
            if (enabled) {
                notifyItemInserted(changingPosition)
                enabledTypeList.add(index, pair.first)
                viewAddition++
            } else {
                notifyItemRemoved(changingPosition)
                enabledTypeList.removeAt(index)
                viewAddition--
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (itemList.lastIndex >= position) {
            RecyclerView.INVALID_TYPE
        } else {
            enabledTypeList[position - itemList.size]
        }
    }

    override fun onBindViewHolder(holder: ReadableMoreViewHolder<T>, position: Int) {
        if (itemList.lastIndex >= position)
            holder.bind(itemList[position])
    }


    override fun getItemCount() = itemList.size + viewAddition

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadableMoreViewHolder<T> {
        return when (viewType) {
            TYPE_READ_MORE.first -> FooterViewHolder(FooterViewUI(parent.context), readableMore ?: throw IllegalStateException("readableMore is not initialized"),
                    autoDisposable)
            TYPE_EMPTY.first -> EmptyViewHolder(parent.context)
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

    fun removeIf(predicate: (T) -> Boolean) {
        itemList.indexesIf(predicate).forEach {
            itemList.removeAt(it)
            notifyItemRemoved(it)
        }
    }

    companion object {
        val TYPE_READ_MORE = 300 to 0
        val TYPE_EMPTY = 400 to 1
    }

    class EmptyViewHolder<in RMT>(ctx: Context) : ReadableMoreViewHolder<RMT>(EmptyViewUI(ctx), {}) {
        override val autoDisposable: AutoDisposable
            get() = throw IllegalStateException()

        override fun bind(value: RMT) {
        }
    }
}

fun <T : Cacheable<Long>> ReadableMoreRecyclerAdapter<T>.remove(id: Long) = removeIf { it.id == id }


abstract class ReadableMoreViewHolder<in T>(ui: UI, protected val onClick: (T) -> Unit) : RecyclerView.ViewHolder(ui.createView()) {
    abstract protected val autoDisposable: AutoDisposable
    abstract fun bind(value: T)
}