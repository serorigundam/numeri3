package tech.ketc.numeri.util.anko

import android.app.Activity
import android.graphics.drawable.Drawable
import android.support.annotation.AttrRes
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import org.jetbrains.anko.*
import tech.ketc.numeri.util.android.getResourceId

fun <T : android.view.View> T.collapsingToolbarlparams(width: kotlin.Int = matchParent, height: kotlin.Int = wrapContent, init: android.support.design.widget.CollapsingToolbarLayout.LayoutParams.() -> kotlin.Unit = {}): T {
    val layoutParams = android.support.design.widget.CollapsingToolbarLayout.LayoutParams(width, height)
    layoutParams.init()
    this.layoutParams = layoutParams
    return this
}

fun <T : android.view.View> T.textInputlparams(width: kotlin.Int = matchParent, height: kotlin.Int = wrapContent, init: FrameLayout.LayoutParams.() -> kotlin.Unit = {}): T {
    val layoutParams = FrameLayout.LayoutParams(width, height)
    layoutParams.init()
    this.layoutParams = layoutParams
    return this
}

fun RelativeLayout.LayoutParams.startOf(id: Int) = leftOf(id)

fun RelativeLayout.LayoutParams.endOf(id: Int) = rightOf(id)

fun AnkoContext<*>.drawable(@DrawableRes id: Int): Drawable = ctx.getDrawable(id)
fun AnkoContext<*>.color(@ColorRes id: Int): Int = ctx.getColor(id)
fun AnkoContext<*>.string(@StringRes id: Int): String = ctx.getString(id)
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

inline fun <T : Activity> AnkoContext<T>.create(build: AnkoContext<T>.() -> Unit) = apply(build).view
