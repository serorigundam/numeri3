package net.ketc.numeri.util.android

import android.view.View
import android.view.ViewGroup

fun ViewGroup.toList(): List<View> = ArrayList<View>().apply {
    for (i in 0..(childCount - 1)) add(getChildAt(i))
}