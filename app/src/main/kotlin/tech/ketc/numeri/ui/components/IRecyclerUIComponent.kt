package tech.ketc.numeri.ui.components

import android.support.v7.widget.RecyclerView
import tech.ketc.numeri.util.anko.UIComponent

interface IRecyclerUIComponent : UIComponent<RecyclerView> {
    val recycler: RecyclerView
}