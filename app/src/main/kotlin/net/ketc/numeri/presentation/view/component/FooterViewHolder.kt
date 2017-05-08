package net.ketc.numeri.presentation.view.component

import android.view.View
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.component.adapter.ReadableMoreViewHolder
import net.ketc.numeri.presentation.view.component.ui.footer.IFooterViewUI
import net.ketc.numeri.util.android.getResourceId
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.MySchedulers

class FooterViewHolder<T, in RMT>(ui: IFooterViewUI, private val readableMore: ReadableMore<T>,
                                  override val autoDisposable: AutoDisposable)
    : ReadableMoreViewHolder<RMT>(ui, {}),
        AutoDisposable by autoDisposable,
        IFooterViewUI by ui {
    override fun bind(value: RMT) {

    }

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
            readMoreText.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            itemView.isClickable = false
        } else {
            readMoreText.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
            itemView.isClickable = true
        }
    }
}

interface ReadableMore<T> {
    fun read(): T
    fun error(throwable: Throwable)
    fun complete(t: T)
}