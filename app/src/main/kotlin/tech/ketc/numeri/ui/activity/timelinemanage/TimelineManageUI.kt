package tech.ketc.numeri.ui.activity.timelinemanage

import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.Gravity
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import tech.ketc.numeri.R
import tech.ketc.numeri.ui.view.pager.nonScrollableViewPager
import tech.ketc.numeri.util.anko.create

class TimelineManageUI : ITimelineManageUI {
    override lateinit var toolbar: Toolbar
        private set
    override lateinit var pager: ViewPager
        private set
    override lateinit var fab: FloatingActionButton
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

            nonScrollableViewPager {
                pager = this
                id = R.id.pager
            }.lparams(matchParent, matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }

            floatingActionButton {
                fab = this
                imageResource = R.drawable.ic_add_white_24dp
                size = FloatingActionButton.SIZE_AUTO
            }.lparams {
                margin = dimen(R.dimen.margin_medium)
                anchorGravity = Gravity.END or Gravity.BOTTOM
                anchorId = R.id.pager
            }
        }
    }
}