package tech.ketc.numeri.ui.components

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import org.jetbrains.anko.below
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.support.v4.viewPager
import tech.ketc.numeri.R

class ScrollableTabPagerComponent : IScrollableTabPagerComponent {
    override lateinit var tab: TabLayout
        private set
    override lateinit var pager: ViewPager
        private set

    override fun createView(ctx: Context) = ctx.relativeLayout {
        lparams(matchParent, matchParent)
        tabLayout {
            tab = this
            id = R.id.tab
            elevation = 5.toFloat()
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