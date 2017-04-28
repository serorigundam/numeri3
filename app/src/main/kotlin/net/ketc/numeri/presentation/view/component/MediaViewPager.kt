package net.ketc.numeri.presentation.view.component

import android.content.Context
import android.view.MotionEvent
import android.view.ViewManager
import com.github.chrisbanes.photoview.PhotoView
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.support.v4._ViewPager


class MediaViewPager(context: Context) : _ViewPager(context) {
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            return false
        }
    }
}

inline fun ViewManager.mediaViewPager(theme: Int = 0, init: _ViewPager.() -> Unit): android.support.v4.view.ViewPager {
    return ankoView(::MediaViewPager, theme) { init() }
}