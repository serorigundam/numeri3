package net.ketc.numeri.presentation.view.component.adapter

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import net.ketc.numeri.domain.entity.TweetsDisplayGroup
import net.ketc.numeri.domain.service.TweetsDisplayService
import net.ketc.numeri.presentation.view.fragment.TimeLineFragment
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.MySchedulers
import java.util.*

class TweetsDisplayPagerAdapter(private val fm: android.support.v4.app.FragmentManager,
                                private val group: net.ketc.numeri.domain.entity.TweetsDisplayGroup,
                                private val autoDisposable: net.ketc.numeri.util.rx.AutoDisposable,
                                private val tweetsDisplayService: net.ketc.numeri.domain.service.TweetsDisplayService) : android.support.v4.app.FragmentStatePagerAdapter(fm) {

    private val fragments = java.util.ArrayList<android.support.v4.app.Fragment>()
    private val previousFragmentIds = java.util.ArrayList<Int>()

    override fun getItem(position: Int): android.support.v4.app.Fragment = fragments[position]

    override fun getCount(): Int = fragments.count()

    override fun getPageTitle(position: Int): CharSequence
            = (fragments[position] as net.ketc.numeri.presentation.view.fragment.TimeLineFragment).displayName

    private var initialized = false

    fun initialize() {
        autoDisposable.singleTask(net.ketc.numeri.util.rx.MySchedulers.twitter) {
            createFragments()
        } error {

        } success {
            fragments.addAll(it)
            previousFragmentIds.addAll(fragments.map { (it as net.ketc.numeri.presentation.view.fragment.TimeLineFragment).display.id })
            notifyDataSetChanged()
            initialized = true
        }
    }

    override fun restoreState(state: android.os.Parcelable?, loader: ClassLoader) {
        super.restoreState(state, loader)
        if (state != null) {
            val bundle = state as android.os.Bundle
            val keys = bundle.keySet()
            keys.forEach {
                if (it.startsWith("f")) {
                    val index = Integer.parseInt(it.substring(1))
                    val fragment = fm.getFragment(bundle, it)
                    fragment?.let {
                        fragments.add(index, it)
                        previousFragmentIds.add((it as net.ketc.numeri.presentation.view.fragment.TimeLineFragment).display.id)
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemPosition(`object`: Any?): Int {
        val unchanged = previousFragmentIds
                .any {
                    it == (`object` as net.ketc.numeri.presentation.view.fragment.TimeLineFragment).display.id &&
                            previousFragmentIds.indexOf(it) == fragments.indexOf(`object`)
                }
        return if (unchanged) {
            POSITION_UNCHANGED
        } else {
            POSITION_NONE
        }
    }

    fun onResume() {
        if (!initialized)
            return
        autoDisposable.singleTask(net.ketc.numeri.util.rx.MySchedulers.twitter) {
            val displays = tweetsDisplayService.getDisplays(group)
            var isChange = fragments.size != displays.size
            if (!isChange) {
                displays.forEachIndexed { i, display ->
                    if (i > fragments.lastIndex || (fragments[i] as net.ketc.numeri.presentation.view.fragment.TimeLineFragment).display.id != display.id) {
                        isChange = true
                        return@forEachIndexed
                    }
                }
            }
            if (isChange) {
                val fragments = displays.mapIndexed { i, display ->
                    (fragments.mapIndexed { i, fragment -> i to fragment as net.ketc.numeri.presentation.view.fragment.TimeLineFragment }
                            .firstOrNull { pair ->
                                pair.first == i && display.id == pair.second.display.id
                            }?.second ?:  net.ketc.numeri.presentation.view.fragment.TimeLineFragment.Companion.create(display))
                }
                true to fragments
            } else {
                false to emptyList()
            }
        } error Throwable::printStackTrace success {
            if (it.first) {
                fragments.clear()
                this.fragments.addAll(it.second)
                notifyDataSetChanged()
                previousFragmentIds.clear()
                previousFragmentIds.addAll(fragments.map { (it as TimeLineFragment).display.id })
            }
        }
    }


    private fun createFragments(): List<TimeLineFragment>
            = tweetsDisplayService.getDisplays(group).map {  TimeLineFragment.create(it) }

}