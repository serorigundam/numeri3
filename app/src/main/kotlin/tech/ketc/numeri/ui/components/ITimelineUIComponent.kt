package tech.ketc.numeri.ui.components

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.widget.RelativeLayout
import tech.ketc.numeri.util.anko.UIComponent

interface ITimelineUIComponent : UIComponent<RelativeLayout> {
    val swipeRefresh: SwipeRefreshLayout
    val timelineRecycler: RecyclerView
}