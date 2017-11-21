package tech.ketc.numeri.ui.activity.timelinemanage

import android.support.design.widget.AppBarLayout
import android.support.v7.widget.Toolbar
import android.view.View
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent
import tech.ketc.numeri.util.anko.create

class TimelineManageUI : ITimelineManageUI {
    override lateinit var toolbar: Toolbar
        private set
    override lateinit var fragmentView: View
        private set

    override fun createView(ui: AnkoContext<TimelineManageActivity>) = ui.create {
        coordinatorLayout {
            appBarLayout {
                toolbar {
                    toolbar = this
                }.lparams(matchParent, wrapContent) {
                    scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                }
            }.lparams(matchParent, wrapContent)
            coordinatorLayout {
                fragmentView = this
            }.lparams(matchParent, matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
        }
    }
}