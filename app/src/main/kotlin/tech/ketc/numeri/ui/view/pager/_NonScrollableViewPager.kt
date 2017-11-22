package tech.ketc.numeri.ui.view.pager

import android.support.v4.view.ViewPager
import android.view.ViewGroup
import android.view.ViewManager
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.AnkoException
import org.jetbrains.anko.AnkoViewDslMarker

fun ViewManager.nonScrollableViewPager(init: (@AnkoViewDslMarker ViewPager).() -> Unit) = when (this) {
    is ViewGroup -> NonScrollableViewPager(context).apply(init).also { addView(it) }
    is AnkoContext<*> -> NonScrollableViewPager(ctx).apply(init).also { addView(it, null) }
    else -> throw AnkoException("$this is the wrong parent")
}