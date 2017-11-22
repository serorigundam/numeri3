package tech.ketc.numeri.util.android.ui.recycler

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View


class SimpleItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val mDivider: Drawable

    init {
        val attr: TypedArray = context.obtainStyledAttributes(arrayOf(android.R.attr.listDivider).toIntArray())
        mDivider = attr.getDrawable(0)
        attr.recycle()
    }


    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        for (i in 0 until parent.childCount) {
            val view: View = parent.getChildAt(i)
            val params = view.layoutParams as RecyclerView.LayoutParams
            val top = view.bottom + params.bottomMargin
            val bottom = top + mDivider.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }
}