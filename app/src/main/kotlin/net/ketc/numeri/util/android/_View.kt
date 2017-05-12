package net.ketc.numeri.util.android

import android.view.View

fun View.makeSimpleClickable() {
    val resourceId = context.getResourceId(android.R.attr.selectableItemBackground)
    background = context.getDrawable(resourceId)
    isClickable = true
}