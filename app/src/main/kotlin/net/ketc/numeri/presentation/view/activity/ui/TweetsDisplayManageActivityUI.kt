package net.ketc.numeri.presentation.view.activity.ui

import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.activity.TweetsDisplayManageActivity
import net.ketc.numeri.util.android.getResourceId
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.drawerLayout
import org.jetbrains.anko.support.v4.nestedScrollView

class TweetsDisplayManageActivityUI : AnkoComponent<TweetsDisplayManageActivity> {
    override fun createView(ui: AnkoContext<TweetsDisplayManageActivity>): View = with(ui) {
        drawerLayout {
            id = R.id.drawer
            lparams(matchParent, matchParent)
            coordinatorLayout {
                appBarLayout {
                    toolbar {
                        id = R.id.toolbar
                    }.lparams(matchParent, wrapContent) {
                        scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    }
                }.lparams(matchParent, wrapContent)
                recyclerView {
                    id = R.id.displays_recycler
                }.lparams(matchParent, matchParent) {
                    behavior = AppBarLayout.ScrollingViewBehavior()
                }
                floatingActionButton {
                    id = R.id.add_fab
                    image = ctx.getDrawable(R.drawable.ic_add_white_24dp)
                    size = FloatingActionButton.SIZE_AUTO
                }.lparams {
                    margin = dimen(R.dimen.margin_medium)
                    anchorGravity = Gravity.BOTTOM or Gravity.END
                    anchorId = R.id.displays_recycler
                }
            }

            relativeLayout {
                id = R.id.navigation
                backgroundColor = context.getColor(context.getResourceId(android.R.attr.colorBackground))
                nestedScrollView {
                    linearLayout {
                        id = R.id.navigation_content
                        lparams(dip(320), matchParent)
                        orientation = LinearLayout.VERTICAL
                    }
                }.lparams(wrapContent, wrapContent)
            }.lparams(dip(320), matchParent) {
                gravity = Gravity.START
            }

        }
    }
}
