package net.ketc.numeri.presentation.view.component

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.ketc.numeri.R
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.cache.Cacheable
import net.ketc.numeri.presentation.view.component.ui.FooterView
import net.ketc.numeri.presentation.view.component.ui.FooterViewUI
import net.ketc.numeri.util.android.getResourceId
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.MySchedulers
import org.jetbrains.anko.*
import java.util.*

class TwitterRecyclerAdapter<T : Cacheable<Long>>(private val readableMore: ReadableMore<MutableList<T>>,
                                                  private val autoDisposable: AutoDisposable,
                                                  private val create: () -> TwitterViewHolder<T>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val itemList = ArrayList<T>()
    val last: T?
        get() = itemList.lastOrNull()
    val first: T?
        get() = itemList.firstOrNull()

    override fun getItemViewType(position: Int): Int {
        return if (itemList.lastIndex >= position) RecyclerView.INVALID_TYPE
        else TYPE_FOOTER
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //ダサい…
        if (itemList.lastIndex >= position)
            when (holder) {
                is TweetViewHolder -> holder.bind(itemList[position] as Tweet)
                else -> throw InternalError()
            }
    }

    override fun getItemCount() = itemList.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_FOOTER -> FooterViewHolder(readableMore, autoDisposable, parent.context)
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
        if (index == -1) {
            itemList.remove(t)
            notifyItemRemoved(index)
        }
    }

    fun remove(id: Long) {
        val removed = itemList.first { it.id == id }
        remove(removed)
    }

    companion object {
        val TYPE_FOOTER = 300
    }
}

abstract class TwitterViewHolder<in T : Cacheable<Long>>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(cacheable: T)
}

class TweetViewHolder(ctx: Context) : TwitterViewHolder<Tweet>(ctx.relativeLayout {
    textView {
        id = R.id.tweet_text
    }.lparams(matchParent, wrapContent)
}) {
    override fun bind(cacheable: Tweet) {
        tweetText.text = "screenName =${cacheable.user.screenName}, name = ${cacheable.user.name}\n ,text = ${cacheable.text}"
    }

    private val tweetText: TextView by lazy { itemView.find<TextView>(R.id.tweet_text) }

}

class FooterViewHolder<T>(private val readableMore: ReadableMore<T>,
                          private val autoDisposable: AutoDisposable,
                          ctx: Context) : RecyclerView.ViewHolder(FooterViewUI(ctx).createView()), FooterView, AutoDisposable by autoDisposable {
    init {
        val context = itemView.context
        val resourceId = context.getResourceId(android.R.attr.selectableItemBackground)
        val drawable = context.getDrawable(resourceId)
        itemView.background = drawable
        itemView.isClickable = true
        itemView.setOnClickListener { onClick() }
    }

    private fun onClick() {
        setProgress(true)
        singleTask(MySchedulers.twitter) {
            itemView.isClickable = false
            readableMore.read()
        } error {
            readableMore.error(it)
            itemView.isClickable = true
            setProgress(false)
        } success {
            readableMore.complete(it)
            itemView.isClickable = true
            setProgress(false)
        }
    }

    fun setProgress(progress: Boolean) {
        if (progress) {
            itemView.readMoreText.visibility = View.INVISIBLE
            itemView.progressBar.visibility = View.VISIBLE
            itemView.isClickable = false
        } else {
            itemView.readMoreText.visibility = View.VISIBLE
            itemView.progressBar.visibility = View.INVISIBLE
            itemView.isClickable = true
        }
    }

}

interface ReadableMore<T> {
    fun read(): T
    fun error(throwable: Throwable)
    fun complete(t: T)
}