package tech.ketc.numeri.ui.activity.timelinemanage

import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import org.jetbrains.anko.AnkoComponent

interface ITimelineManageUI : AnkoComponent<TimelineManageActivity> {
    val toolbar: Toolbar
    val pager: ViewPager
    val fab: FloatingActionButton
}
