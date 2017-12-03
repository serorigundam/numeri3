package tech.ketc.numeri.util.android.ui.recycler

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import org.jetbrains.anko.*
import tech.ketc.numeri.R
import tech.ketc.numeri.util.android.ui.enableRippleEffect

class ProgressViewHolder(context: Context) : RecyclerView.ViewHolder(context.relativeLayout {
    lparams(matchParent, wrapContent)
    textView {
        id = R.id.read_more_text
        textSize = 16F
        text = context.getString(R.string.read_more)
        gravity = Gravity.CENTER
    }.lparams(matchParent, wrapContent) {
        margin = dimen(R.dimen.margin_medium)
    }

    progressBar {
        id = R.id.progress_bar
        visibility = View.INVISIBLE
    }.lparams(dimen(R.dimen.progress_bar_size),
            dimen(R.dimen.progress_bar_size)) {
        centerInParent()
        scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
    }
}) {
    private val text: TextView
    private val progress: ProgressBar

    init {
        itemView.enableRippleEffect()
        text = itemView.findViewById(R.id.read_more_text)
        progress = itemView.findViewById(R.id.progress_bar)
    }

    fun setProgress(isProgress: Boolean) {
        if (isProgress) {
            text.visibility = View.INVISIBLE
            progress.visibility = View.VISIBLE
            itemView.isClickable = false
        } else {
            text.visibility = View.VISIBLE
            progress.visibility = View.INVISIBLE
            itemView.isClickable = true
        }
    }
}