package tech.ketc.numeri.ui.components

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import tech.ketc.numeri.R
import tech.ketc.numeri.util.android.ui.recycler.simpleInit

class SwipeRefreshRecyclerComponent : ISwipeRefreshRecyclerComponent {
    override lateinit var swipeRefresh: SwipeRefreshLayout
        private set
    override lateinit var recycler: RecyclerView
        private set

    override fun createView(ctx: Context) = ctx.relativeLayout {
        lparams(matchParent, matchParent)
        swipeRefreshLayout {
            swipeRefresh = this
            recyclerView {
                recycler = this
                id = R.id.recycler
                lparams(matchParent, matchParent)
                isVerticalScrollBarEnabled = true
                simpleInit()
            }
        }.lparams(matchParent, matchParent)
    }
}