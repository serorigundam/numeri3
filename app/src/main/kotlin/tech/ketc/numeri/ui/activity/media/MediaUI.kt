package tech.ketc.numeri.ui.activity.media

import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import tech.ketc.numeri.R
import tech.ketc.numeri.ui.view.media.mediaViewPager
import tech.ketc.numeri.util.anko.color
import tech.ketc.numeri.util.anko.create
import tech.ketc.numeri.util.anko.marginTop

class MediaUI : IMediaUI {
    override lateinit var componentRoot: View
        private set
    override lateinit var appBar: AppBarLayout
        private set
    override lateinit var toolbar: Toolbar
        private set
    override lateinit var pager: ViewPager
        private set

    override fun createView(ui: AnkoContext<MediaActivity>) = ui.create {
        relativeLayout {
            lparams(matchParent, matchParent)
            backgroundColor = color(R.color.transparent)
            componentRoot = this
            appBarLayout {
                appBar = this
                backgroundColor = color(R.color.app_bar_transparent)
                toolbar {
                    toolbar = this
                    backgroundColor = color(R.color.app_bar_transparent)
                }.lparams(matchParent, wrapContent) {
                    marginTop = dimen(R.dimen.status_bar_height)
                }
            }.lparams(matchParent, wrapContent)
            mediaViewPager {
                pager = this
                id = R.id.pager
                offscreenPageLimit = 4
            }.lparams(matchParent, matchParent)
        }
    }
}