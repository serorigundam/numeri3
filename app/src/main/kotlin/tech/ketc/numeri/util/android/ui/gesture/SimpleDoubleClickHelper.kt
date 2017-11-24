package tech.ketc.numeri.util.android.ui.gesture

import android.os.Handler
import android.view.View

class SimpleDoubleClickHelper(private val mWaitMills: Long = 250L, onDoubleClick: () -> Unit = {}, onClick: () -> Unit) {
    private var mDouble = false
    private var mSingle = false
    private val mHandler = Handler()
    private val mMultiTapFunc: () -> Unit = {
        if (mDouble)
            onDoubleClick()
        else
            onClick()
        mDouble = false
        mSingle = false
        remove()
    }

    private fun remove() {
        mHandler.removeCallbacks(mMultiTapFunc)
    }

    fun attachTo(view: View) {
        view.setOnClickListener {
            if (!mSingle && !mDouble)
                mHandler.postDelayed(mMultiTapFunc, mWaitMills)
            if (mSingle) {
                mDouble = true
            }
            mSingle = true
        }
    }
}