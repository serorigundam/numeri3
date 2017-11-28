package tech.ketc.numeri.ui.view.pager

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter

class SimplePagerAdapter(private val mFm: FragmentManager)
    : FragmentStatePagerAdapter(mFm) {

    private val mContents = ArrayList<Fragment>()

    fun setContents(vararg fragment: Fragment) {
        mContents.addAll(fragment)
    }

    override fun getItem(position: Int) = mContents[position]

    override fun getCount() = mContents.size

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        super.restoreState(state, loader)
        if (state != null) {
            val bundle = state as Bundle
            val keys = bundle.keySet()
            keys.forEach { key ->
                key.takeIf { it.startsWith("f") }?.let {
                    val fragment = mFm.getFragment(bundle, it)
                    fragment?.let { mContents.add(it) }
                }
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemPosition(`object`: Any): Int {
        val fragment = `object` as? Fragment ?: return PagerAdapter.POSITION_NONE
        return if (mContents.any { it == fragment })
            PagerAdapter.POSITION_UNCHANGED
        else PagerAdapter.POSITION_NONE
    }
}

