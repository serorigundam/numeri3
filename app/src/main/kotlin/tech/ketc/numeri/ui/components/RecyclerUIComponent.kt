package tech.ketc.numeri.ui.components

import android.content.Context
import android.support.v7.widget.RecyclerView
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import tech.ketc.numeri.R
import tech.ketc.numeri.util.android.ui.recycler.simpleInit

class RecyclerUIComponent : IRecyclerUIComponent {
    override lateinit var recycler: RecyclerView
    override fun createView(ctx: Context) = ctx.recyclerView {
        lparams(matchParent, matchParent)
        recycler = this
        id = R.id.recycler
        simpleInit()
    }
}