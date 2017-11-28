package tech.ketc.numeri.ui.view.media

import android.content.Context
import android.view.MotionEvent
import org.jetbrains.anko.support.v4._ViewPager

class MediaViewPager(context: Context) : _ViewPager(context) {
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return try {
            super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}