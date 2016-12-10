package net.ketc.numeri.util.android

import android.content.Context
import android.util.TypedValue

fun Context.getResourceId(resId: Int): Int {
    val outValue = TypedValue()
    theme.resolveAttribute(resId, outValue, true)
    return outValue.resourceId
}