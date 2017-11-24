package tech.ketc.numeri.ui.components

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.widget.RelativeLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.support.v4.viewPager
import tech.ketc.numeri.R
import tech.ketc.numeri.util.android.getResourceId

class ScrollableTabPagerUIComponent : IScrollableTabPagerUIComponent {
    override lateinit var componentRoot: RelativeLayout
        private set
    override lateinit var tab: TabLayout
        private set
    override lateinit var pager: ViewPager
        private set

    override fun createView(ctx: Context) = ctx.relativeLayout {
        componentRoot = this
        lparams(matchParent, matchParent)
        tabLayout {
            tab = this
            id = R.id.tab
            elevation = 5.toFloat()
            backgroundColor = ctx.getColor(ctx.getResourceId(android.R.attr.colorBackground))
            tabMode = TabLayout.MODE_SCROLLABLE
        }.lparams(matchParent, dip(32))

        viewPager {
            pager = this
            id = R.id.pager
            offscreenPageLimit = 10
        }.lparams(matchParent, matchParent) {
            below(R.id.tab)
        }
    }
}