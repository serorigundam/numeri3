package net.ketc.numeri.presentation.view.activity.ui

import android.support.design.widget.AppBarLayout
import android.support.v7.widget.Toolbar
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.activity.UserInfoActivity
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.collapsingToolbarLayout
import org.jetbrains.anko.design.coordinatorLayout
import android.support.design.widget.AppBarLayout.LayoutParams.*
import android.support.design.widget.CollapsingToolbarLayout
import net.ketc.numeri.util.android.collapsingToolbarlparams
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.nestedScrollView

class UserInfoActivityUI : IUserInfoActivityUI {
    override lateinit var toolbar: Toolbar
        private set

    override fun createView(ui: AnkoContext<UserInfoActivity>) = with(ui) {
        coordinatorLayout {
            lparams(matchParent, matchParent)
            appBarLayout {
                collapsingToolbarLayout {
                    toolbar {
                        toolbar = this
                        setContentScrimResource(R.color.colorPrimary)
                    }.collapsingToolbarlparams {
                        collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
                    }
                }.lparams(matchParent, matchParent) {
                    scrollFlags = SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
                }
            }.lparams(matchParent, wrapContent)

            nestedScrollView {
                textView {
                    text = (0..100).map(Int::toString).joinToString("\n")
                }
            }.lparams(matchParent, wrapContent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
        }
    }
}

interface IUserInfoActivityUI : AnkoComponent<UserInfoActivity> {
    val toolbar: Toolbar
}
