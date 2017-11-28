package tech.ketc.numeri.ui.activity.conversation

import android.support.design.widget.AppBarLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.wrapContent
import tech.ketc.numeri.util.android.ui.recycler.simpleInit
import tech.ketc.numeri.util.anko.create

class ConversationUI : IConversationUI {
    override lateinit var toolbar: Toolbar
        private set
    override lateinit var recycler: RecyclerView
        private set

    override fun createView(ui: AnkoContext<ConversationActivity>) = ui.create {
        coordinatorLayout {
            appBarLayout {
                toolbar {
                    toolbar = this
                }.lparams(matchParent, wrapContent) {
                    scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                }
            }.lparams(matchParent, wrapContent)

            recyclerView {
                recycler = this
                simpleInit()
            }.lparams(matchParent, matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
        }
    }
}