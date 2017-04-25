package net.ketc.numeri.presentation.view.activity.ui

import android.graphics.Color
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.activity.MediaActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.support.v4.viewPager

class MediaActivityUI : IMediaActivityUI {
    override lateinit var toolbar: Toolbar
        private set
    override lateinit var pager: ViewPager
        private set


    override fun createView(ui: AnkoContext<MediaActivity>) = with(ui) {
        coordinatorLayout {
            fitsSystemWindows = true
            appBarLayout {
                backgroundColor = ctx.getColor(R.color.app_bar_transparent)
                toolbar {
                    toolbar = this
                }.lparams(matchParent, wrapContent)
            }.lparams(matchParent, wrapContent)
            viewPager {
                fitsSystemWindows = true
                pager = this
                offscreenPageLimit = 4
                backgroundColor = Color.BLACK
            }.lparams(matchParent, matchParent)
        }
    }
}

interface IMediaActivityUI : AnkoComponent<MediaActivity> {
    val toolbar: Toolbar
    val pager: ViewPager
}
