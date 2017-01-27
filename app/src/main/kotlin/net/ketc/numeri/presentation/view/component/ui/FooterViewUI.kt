package net.ketc.numeri.presentation.view.component.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import net.ketc.numeri.R
import org.jetbrains.anko.*

class FooterViewUI(override val ctx: Context) : UI {
    override fun createView(): View = ctx.relativeLayout {
        lparams(matchParent, wrapContent)
        textView {
            id = R.id.read_more_text
            textSize = 16F
            text = ctx.getString(R.string.read_more)
            gravity = Gravity.CENTER
        }.lparams(matchParent, wrapContent) {
            margin = dimen(R.dimen.margin_medium)
        }

        progressBar {
            id = R.id.progress_bar
            visibility = View.INVISIBLE
        }.lparams(dimen(R.dimen.progress_bar_size), dimen(R.dimen.progress_bar_size)) {
            centerInParent()
            scrollBarStyle = R.style.ProgressBar
        }
    }
}

val View.readMoreText: TextView
    get() = find(R.id.read_more_text)
val View.progressBar: ProgressBar
    get() = find(R.id.progress_bar)
