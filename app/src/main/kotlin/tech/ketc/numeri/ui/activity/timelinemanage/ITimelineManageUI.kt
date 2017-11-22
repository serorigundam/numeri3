package tech.ketc.numeri.ui.activity.timelinemanage

import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.View
import org.jetbrains.anko.AnkoComponent

interface ITimelineManageUI : AnkoComponent<TimelineManageActivity> {
    val toolbar: Toolbar
    val fragmentView: View
    val fab: FloatingActionButton
}
