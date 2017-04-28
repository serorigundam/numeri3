package net.ketc.numeri.presentation.view.component.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class SimplePagerAdapter(supportFragmentManager: FragmentManager, private val fragmentList: List<Fragment>)
    : FragmentPagerAdapter(supportFragmentManager) {

    override fun getItem(position: Int) = fragmentList[position]

    override fun getCount() = fragmentList.size
}