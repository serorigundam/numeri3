package tech.ketc.numeri.util.android.ui.recycler

import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

fun RecyclerView.simpleInit() {
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    itemAnimator = DefaultItemAnimator()
    addItemDecoration(SimpleItemDecoration(context))
}