package net.ketc.numeri.util.android

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper


class SimpleItemTouchHelper(moveEnable: Boolean = false,
                            swipeEnable: Boolean = false,
                            onMove: (recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) -> Boolean = { _, _, _ -> false },
                            onSwiped: (viewHolder: RecyclerView.ViewHolder, direction: Int) -> Unit = { _, _ -> }) :
        ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(
                        if (moveEnable) ItemTouchHelper.DOWN or ItemTouchHelper.UP else 0,
                        if (swipeEnable) ItemTouchHelper.START or ItemTouchHelper.END else 0) {

                    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                        return onMove(recyclerView, viewHolder, target)
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        onSwiped(viewHolder, direction)
                    }
                })