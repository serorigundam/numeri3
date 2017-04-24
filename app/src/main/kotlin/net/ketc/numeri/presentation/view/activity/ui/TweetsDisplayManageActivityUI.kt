package net.ketc.numeri.presentation.view.activity.ui

import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.activity.TweetsDisplayManageActivity
import net.ketc.numeri.util.android.getResourceId
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.drawerLayout
import org.jetbrains.anko.support.v4.nestedScrollView

class TweetsDisplayManageActivityUI : ITweetsDisplayManageActivityUI {

    override lateinit var drawer: DrawerLayout
        private set
    override lateinit var toolbar: Toolbar
        private set
    override lateinit var displaysRecycler: RecyclerView
        private set
    override lateinit var navigation: RelativeLayout
        private set
    override lateinit var navigationContent: LinearLayout
        private set
    override lateinit var addButton: FloatingActionButton
        private set

    override fun createView(ui: AnkoContext<TweetsDisplayManageActivity>): View = with(ui) {
        drawerLayout {
            drawer = this
            id = R.id.drawer
            lparams(matchParent, matchParent)
            coordinatorLayout {
                appBarLayout {
                    toolbar {
                        toolbar = this
                        id = R.id.toolbar
                    }.lparams(matchParent, wrapContent) {
                        scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    }
                }.lparams(matchParent, wrapContent)
                recyclerView {
                    displaysRecycler = this
                    id = R.id.displays_recycler
                }.lparams(matchParent, matchParent) {
                    behavior = AppBarLayout.ScrollingViewBehavior()
                }
                floatingActionButton {
                    addButton = this
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
                navigation = this
                backgroundColor = context.getColor(context.getResourceId(android.R.attr.colorBackground))
                nestedScrollView {
                    linearLayout {
                        navigationContent = this
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

interface ITweetsDisplayManageActivityUI : AnkoComponent<TweetsDisplayManageActivity> {
    val drawer: DrawerLayout
    val toolbar: Toolbar
    val displaysRecycler: RecyclerView
    val navigation: RelativeLayout
    val navigationContent: LinearLayout
    val addButton: FloatingActionButton
}
