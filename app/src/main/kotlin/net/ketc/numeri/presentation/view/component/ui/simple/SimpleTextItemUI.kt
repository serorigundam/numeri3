package net.ketc.numeri.presentation.view.component.ui.simple

import android.content.Context
import android.view.View
import android.widget.TextView
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.component.ui.UI
import net.ketc.numeri.util.android.getResourceId
import org.jetbrains.anko.*

class SimpleTextItemUI(override val ctx: Context) : ISimpleTextItemUI {
    override lateinit var text: TextView
        private set

    override fun createView(): View = ctx.relativeLayout {
        lparams(matchParent, wrapContent) {
            padding = dimen(R.dimen.margin_medium)
        }
        textView {
            this@SimpleTextItemUI.text = this
            id = R.id.text
            textColor = ctx.getColor(ctx.getResourceId(android.R.attr.textColorPrimary))
        }.lparams(matchParent, wrapContent)
    }
}

interface ISimpleTextItemUI : UI {
    val text: TextView
}