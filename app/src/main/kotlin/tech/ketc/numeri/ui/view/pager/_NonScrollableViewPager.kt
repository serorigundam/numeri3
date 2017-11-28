package tech.ketc.numeri.ui.view.pager

import android.support.v4.view.ViewPager
import android.view.ViewManager
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.support.v4._ViewPager


inline fun ViewManager.nonScrollableViewPager(theme: Int = 0, init: _ViewPager.() -> Unit): ViewPager {
    return ankoView(::NonScrollableViewPager, theme, init)
}