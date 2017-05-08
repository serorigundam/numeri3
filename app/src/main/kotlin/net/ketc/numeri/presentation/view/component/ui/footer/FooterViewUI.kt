package net.ketc.numeri.presentation.view.component.ui.footer

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.component.ui.UI
import org.jetbrains.anko.*

class FooterViewUI(override val ctx: Context) : IFooterViewUI {
    override lateinit var readMoreText: TextView
        private set
    override lateinit var progressBar: ProgressBar
        private set

    override fun createView(): View = ctx.relativeLayout {
        lparams(matchParent, wrapContent)
        textView {
            readMoreText = this
            id = R.id.read_more_text
            textSize = 16F
            text = ctx.getString(R.string.read_more)
            gravity = Gravity.CENTER
        }.lparams(matchParent, wrapContent) {
            margin = dimen(R.dimen.margin_medium)
        }

        progressBar {
            id = R.id.progress_bar
            progressBar = this
            visibility = View.INVISIBLE
        }.lparams(dimen(R.dimen.progress_bar_size), dimen(R.dimen.progress_bar_size)) {
            centerInParent()
            scrollBarStyle = R.style.ProgressBar
        }
    }
}


interface IFooterViewUI : UI {
    val readMoreText: TextView
    val progressBar: ProgressBar
}