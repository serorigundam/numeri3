package net.ketc.numeri.presentation.view.component.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import net.ketc.numeri.presentation.view.SimplePagerContent
import net.ketc.numeri.util.copy

class SimplePagerAdapter(supportFragmentManager: FragmentManager, private val fragmentList: List<Fragment>)
    : FragmentPagerAdapter(supportFragmentManager) {

    override fun getItem(position: Int) = fragmentList[position]

    override fun getCount() = fragmentList.size

    override fun getPageTitle(position: Int) = (fragmentList[position] as? SimplePagerContent)?.contentName ?: ""

    val itemList = fragmentList.copy()

}