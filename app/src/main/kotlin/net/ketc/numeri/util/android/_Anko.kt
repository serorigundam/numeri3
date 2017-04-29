package net.ketc.numeri.util.android

import android.graphics.drawable.Drawable
import android.support.annotation.AttrRes
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.view.ViewGroup
import android.widget.RelativeLayout
import org.jetbrains.anko.*

fun <T : android.view.View> T.collapsingToolbarlparams(width: kotlin.Int = matchParent, height: kotlin.Int = wrapContent, init: android.support.design.widget.CollapsingToolbarLayout.LayoutParams.() -> kotlin.Unit = {}): T {
    val layoutParams = android.support.design.widget.CollapsingToolbarLayout.LayoutParams(width, height)
    layoutParams.init()
    this.layoutParams = layoutParams
    return this
}

fun RelativeLayout.LayoutParams.startOf(id: Int) = leftOf(id)

fun RelativeLayout.LayoutParams.endOf(id: Int) = rightOf(id)

fun AnkoContext<*>.drawable(@DrawableRes id: Int): Drawable = ctx.getDrawable(id)
fun AnkoContext<*>.color(@ColorRes id: Int): Int = ctx.getColor(id)
fun AnkoContext<*>.resourceId(@AttrRes id: Int) = ctx.getResourceId(id)
var ViewGroup.MarginLayoutParams.marginTop: Int
    get() = topMargin
    set(value) {
        topMargin = value
    }

var ViewGroup.MarginLayoutParams.marginBottom: Int
    get() = bottomMargin
    set(value) {
        bottomMargin = value
    }