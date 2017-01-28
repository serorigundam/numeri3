package net.ketc.numeri.presentation.view.component

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.component.ui.footer.FooterViewUI
import net.ketc.numeri.presentation.view.component.ui.footer.progressBar
import net.ketc.numeri.presentation.view.component.ui.footer.readMoreText
import net.ketc.numeri.util.android.getResourceId
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.MySchedulers

class FooterViewHolder<T>(private val readableMore: ReadableMore<T>,
                          private val autoDisposable: AutoDisposable,
                          ctx: Context) : RecyclerView.ViewHolder(FooterViewUI(ctx).createView()), AutoDisposable by autoDisposable {
    init {
        val context = itemView.context
        val resourceId = context.getResourceId(R.attr.selectableItemBackground)
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