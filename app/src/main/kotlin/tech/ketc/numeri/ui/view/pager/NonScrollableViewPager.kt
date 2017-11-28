package tech.ketc.numeri.ui.view.pager

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import org.jetbrains.anko.support.v4._ViewPager

class NonScrollableViewPager(context: Context) : _ViewPager(context) {
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }
}