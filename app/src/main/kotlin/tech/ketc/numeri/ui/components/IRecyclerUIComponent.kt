package tech.ketc.numeri.ui.components

import android.support.v7.widget.RecyclerView
import android.widget.FrameLayout
import tech.ketc.numeri.util.anko.UIComponent

interface IRecyclerUIComponent : UIComponent<FrameLayout> {
    val root: FrameLayout
    val recycler: RecyclerView
}