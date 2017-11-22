package tech.ketc.numeri.ui.view.pager

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class NonScrollableViewPager(context: Context) : ViewPager(context) {
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    inline fun <T : View> T.lparams(init: ViewPager.LayoutParams.() -> Unit): T {
        val layoutParams = ViewPager.LayoutParams()
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(): T {
        val layoutParams = ViewPager.LayoutParams()
        this@lparams.layoutParams = layoutParams
        return this
    }

    inline fun <T : View> T.lparams(context: Context?,
                                    attrs: AttributeSet?,
                                    init: ViewPager.LayoutParams.() -> Unit): T {
        val layoutParams = ViewPager.LayoutParams(context!!, attrs!!)
        layoutParams.init()
        this@lparams.layoutParams = layoutParams
        return this
    }

    fun <T : View> T.lparams(context: Context?,
                             attrs: AttributeSet?): T {
        val layoutParams = ViewPager.LayoutParams(context!!, attrs!!)
        this@lparams.layoutParams = layoutParams
        return this
    }
}