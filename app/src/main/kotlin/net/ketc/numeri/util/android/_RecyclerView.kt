package net.ketc.numeri.util.android

import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

fun RecyclerView.initialize() {
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    itemAnimator = DefaultItemAnimator()
    addItemDecoration(SimpleItemDecoration(context))
}
