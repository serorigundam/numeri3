package tech.ketc.numeri.ui.activity.timeline

import android.support.v7.widget.Toolbar
import android.view.View
import org.jetbrains.anko.AnkoComponent

interface ITimelineManageUI : AnkoComponent<TimelineManageActivity> {
    val toolbar: Toolbar
    val fragmentView: View
}
