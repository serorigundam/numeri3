package net.ketc.numeri.util.android

import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent

fun <T : android.view.View> T.collapsingToolbarlparams(width: kotlin.Int = matchParent, height: kotlin.Int = wrapContent, init: android.support.design.widget.CollapsingToolbarLayout.LayoutParams.() -> kotlin.Unit = {}): T {
    val layoutParams = android.support.design.widget.CollapsingToolbarLayout.LayoutParams(width, height)
    layoutParams.init()
    this.layoutParams = layoutParams
    return this
}