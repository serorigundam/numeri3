package net.ketc.numeri.presentation.view.activity.ui

import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.activity.MediaActivity
import net.ketc.numeri.presentation.view.component.mediaViewPager
import net.ketc.numeri.util.android.color
import net.ketc.numeri.util.android.marginTop
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout

class MediaActivityUI : IMediaActivityUI {
    override lateinit var appBar: AppBarLayout
        private set
    override lateinit var toolbar: Toolbar
        private set
    override lateinit var pager: ViewPager
        private set


    override fun createView(ui: AnkoContext<MediaActivity>) = with(ui) {
        relativeLayout {
            lparams(matchParent, matchParent)
            appBarLayout {
                appBar = this
                backgroundColor = color(R.color.transparent)
                toolbar {
                    toolbar = this
                    backgroundColor = color(R.color.app_bar_transparent)
                }.lparams(matchParent, wrapContent) {
                    marginTop = dimen(R.dimen.status_bar_height)
                }
            }.lparams(matchParent, wrapContent)

            mediaViewPager {
                id = R.id.pager
                pager = this
                offscreenPageLimit = 4
                backgroundColor = Color.BLACK
            }.lparams(matchParent, matchParent)
        }
    }
}

interface IMediaActivityUI : AnkoComponent<MediaActivity> {
    val appBar: AppBarLayout
    val toolbar: Toolbar
    val pager: ViewPager
}
