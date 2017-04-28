package net.ketc.numeri.presentation.view.activity.ui

import android.graphics.Color
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.activity.MediaActivity
import net.ketc.numeri.presentation.view.component.mediaViewPager
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout

class MediaActivityUI : IMediaActivityUI {
    override lateinit var toolbar: Toolbar
        private set
    override lateinit var pager: ViewPager
        private set


    override fun createView(ui: AnkoContext<MediaActivity>) = with(ui) {
        relativeLayout {
            lparams(matchParent, matchParent)
            fitsSystemWindows = true
            appBarLayout {
                backgroundColor = ctx.getColor(R.color.app_bar_transparent)
                toolbar {
                    toolbar = this
                }.lparams(matchParent, wrapContent)
            }.lparams(matchParent, wrapContent)

            mediaViewPager {
                id = R.id.pager
                pager = this
                fitsSystemWindows = true
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
