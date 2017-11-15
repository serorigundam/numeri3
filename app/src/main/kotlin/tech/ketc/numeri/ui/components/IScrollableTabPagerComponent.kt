package tech.ketc.numeri.ui.components

import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.widget.RelativeLayout
import tech.ketc.numeri.util.anko.UIComponent

interface IScrollableTabPagerComponent : UIComponent<RelativeLayout> {
    val tab: TabLayout
    val pager: ViewPager
}