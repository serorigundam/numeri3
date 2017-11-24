package tech.ketc.numeri.ui.components

import android.content.Context
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import org.jetbrains.anko.*
import tech.ketc.numeri.R
import tech.ketc.numeri.util.android.getResourceId
import tech.ketc.numeri.util.anko.initialize1Line

class MenuItemUIComponent : IMenuItemUIComponent {
    override lateinit var componentRoot: RelativeLayout
        private set
    override lateinit var imageView: ImageView
        private set
    override lateinit var textView: TextView
        private set

    override fun createView(ctx: Context) = ctx.relativeLayout {
        componentRoot = this
        lparams(matchParent, dip(48)) {
            padding = dimen(R.dimen.margin_medium)
        }
        background = ctx.getDrawable(ctx.getResourceId(android.R.attr.selectableItemBackground))
        imageView {
            imageView = this
            id = R.id.icon_image
            backgroundColor = ctx.getColor(R.color.transparent)
        }.lparams(dip(16), dip(16)) {
            alignParentStart()
        }
        textView {
            textView = this
            initialize1Line()
        }.lparams(matchParent, wrapContent) {
            marginStart = dip(40)
            alignParentEnd()
            rightOf(R.id.icon_image)
        }
    }
}