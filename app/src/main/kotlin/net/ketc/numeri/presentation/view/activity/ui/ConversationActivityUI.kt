package net.ketc.numeri.presentation.view.activity.ui

import android.support.design.widget.AppBarLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.activity.ConversationActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView

class ConversationActivityUI : IConversationActivityUI {
    override lateinit var toolbar: Toolbar
        private set
    override lateinit var tweetsRecycler: RecyclerView
        private set

    override fun createView(ui: AnkoContext<ConversationActivity>) = with(ui) {
        coordinatorLayout {
            appBarLayout {
                toolbar {
                    id = R.id.toolbar
                    toolbar = this
                }.lparams(matchParent, wrapContent) {
                    scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                }
            }.lparams(matchParent, wrapContent)
            recyclerView {
                id = R.id.users_recycler
                tweetsRecycler = this
                isVerticalScrollBarEnabled = true
            }.lparams(matchParent, matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
        }
    }
}

interface IConversationActivityUI : AnkoComponent<ConversationActivity> {
    val toolbar: Toolbar
    val tweetsRecycler: RecyclerView
}
