package net.ketc.numeri.presentation.view.activity.ui

import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.activity.TweetsDisplayGroupManageActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.recyclerview.v7.recyclerView

class TweetsDisplayGroupManageActivityUI : AnkoComponent<TweetsDisplayGroupManageActivity> {
    lateinit var toolbar: Toolbar
        private set
    lateinit var groupsRecycler: RecyclerView
        private set
    lateinit var addButton: FloatingActionButton
        private set

    override fun createView(ui: AnkoContext<TweetsDisplayGroupManageActivity>): View = with(ui) {
        coordinatorLayout {
            lparams(matchParent, matchParent)
            appBarLayout {
                toolbar = toolbar {
                    id = R.id.toolbar
                }.lparams(matchParent, wrapContent) {
                    scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                }
            }.lparams(matchParent, wrapContent)
            groupsRecycler = recyclerView {
                id = R.id.groups_recycler
            }.lparams(matchParent, matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
            addButton = floatingActionButton {
                id = R.id.add_fab
                image = ctx.getDrawable(R.drawable.ic_add_white_24dp)
                size = FloatingActionButton.SIZE_AUTO
            }.lparams {
                margin = dimen(R.dimen.margin_medium)
                anchorGravity = Gravity.BOTTOM or Gravity.END
                anchorId = R.id.groups_recycler
            }
        }
    }
}