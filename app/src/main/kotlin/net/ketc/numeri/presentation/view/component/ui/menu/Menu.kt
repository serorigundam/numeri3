package net.ketc.numeri.presentation.view.component.ui.menu

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.design.widget.BottomSheetDialog
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import net.ketc.numeri.R
import net.ketc.numeri.util.android.getResourceId
import org.jetbrains.anko.*

fun createIconMenu(ctx: Context, @DrawableRes drawableId: Int, text: String, onClick: (View) -> Unit = {}) = item(ctx, onClick) {
    imageView {
        id = R.id.icon_image
        backgroundColor = ctx.getColor(R.color.transparent)
        image = context.getDrawable(drawableId)
    }.lparams(dip(16), dip(16))
    menuText(R.id.icon_image) {
        this.text = text
    }
}

fun createIconMenu(ctx: Context, @DrawableRes drawableId: Int, @StringRes textId: Int, onClick: (View) -> Unit = {}): View {
    return createIconMenu(ctx, drawableId, ctx.getString(textId), onClick)
}

fun createTextMenu(ctx: Context, menuText: String, text: String, onClick: (View) -> Unit = {}) = item(ctx, onClick) {
    textView {
        id = R.id.text
        backgroundColor = ctx.getColor(R.color.transparent)
        this.text = menuText
        ellipsize = TextUtils.TruncateAt.END
        maxEms = 2
    }.lparams(dip(16), dip(16))
    menuText(R.id.text) {
        this.text = text
    }
}

fun createTextMenu(ctx: Context, menuText: String, @StringRes textId: Int, onClick: (View) -> Unit = {}): View {
    return createTextMenu(ctx, menuText, ctx.getString(textId), onClick)
}

private fun item(ctx: Context, onClick: (View) -> Unit, init: _RelativeLayout.() -> Unit) = ctx.relativeLayout {
    init()
    setOnClickListener { onClick(it) }
    background = ctx.getDrawable(ctx.getResourceId(android.R.attr.selectableItemBackground))
    lparams(matchParent, dip(48)) {
        padding = dimen(R.dimen.margin_medium)
        gravity = Gravity.CENTER
    }
}

val View.iconImage: ImageView
    get() = find(R.id.icon_image)

val View.menuText: TextView
    get() = find(R.id.menu_text)

private fun _RelativeLayout.menuText(rightOf: Int, init: TextView.() -> Unit) = this.textView {
    id = net.ketc.numeri.R.id.menu_text
    ellipsize = android.text.TextUtils.TruncateAt.END
    lines = 1
    textColor = context.getColor(context.getResourceId(android.R.attr.textColorPrimary))
    init()
}.lparams(matchParent, wrapContent) {
    marginStart = dip(40)
    rightOf(rightOf)
}

fun BottomSheetDialog.addTweetMenu(view: View) {
    (this.findViewById(net.ketc.numeri.R.id.menu_linear) as ViewGroup).addView(view)
}

val BottomSheetDialog.tweetText: TextView
    get() = findViewById(R.id.tweet_text)!! as TextView
