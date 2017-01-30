package net.ketc.numeri.presentation.view.component

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import net.ketc.numeri.domain.entity.TweetsDisplay
import net.ketc.numeri.domain.entity.TweetsDisplayGroup
import net.ketc.numeri.domain.entity.TweetsDisplayType
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.model.cache.TwitterUserCache
import net.ketc.numeri.domain.model.cache.convertAndCacheOrGet
import net.ketc.numeri.domain.model.cache.user
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.TweetsDisplayService
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.view.fragment.TimeLineFragment
import net.ketc.numeri.presentation.view.fragment.TimeLineFragmentInterface
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.MySchedulers
import java.util.*

class TweetsDisplayPagerAdapter(private val fm: FragmentManager,
                                private val group: TweetsDisplayGroup,
                                private val autoDisposable: AutoDisposable,
                                private val oAuthService: OAuthService,
                                private val tweetsDisplayService: TweetsDisplayService) : FragmentStatePagerAdapter(fm) {

    private val fragments = ArrayList<Fragment>()
    private val previousFragmentIds = ArrayList<Int>()

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

    override fun getPageTitle(position: Int): CharSequence
            = (fragments[position] as TimeLineFragmentInterface).displayName

    fun initialize() {
        autoDisposable.singleTask(MySchedulers.twitter) {
            createFragments()
        } error {

        } success {
            fragments.addAll(it)
            previousFragmentIds.addAll(fragments.map { it.id })
            notifyDataSetChanged()
        }
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader) {
        super.restoreState(state, loader)
        if (state != null) {
            val bundle = state as Bundle
            val keys = bundle.keySet()
            keys.forEach {
                if (it.startsWith("f")) {
                    val index = Integer.parseInt(it.substring(1))
                    val fragment = fm.getFragment(bundle, it)
                    fragment?.let {
                        fragments.add(index, it)
                        previousFragmentIds.add(it.id)
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemPosition(`object`: Any?): Int {
        if (`object` == null)
            throw IllegalArgumentException("do not accept null")
        val unchanged = previousFragmentIds
                .any {
                    it == (`object` as Fragment).id &&
                            previousFragmentIds.indexOf(it) == fragments.indexOf(`object`)
                }

        return if (unchanged) {
            POSITION_UNCHANGED
        } else {
            POSITION_NONE
        }
    }

    fun onResume() {
        autoDisposable.singleTask(MySchedulers.twitter) {
            oAuthService.clients()
            var isChange = false
            val displays = tweetsDisplayService.getDisplays(group)
            displays.forEachIndexed { i, display ->
                if (i > fragments.lastIndex || (fragments[i] as TimeLineFragment).display.id != display.id) {
                    isChange = true
                    return@forEachIndexed
                }
            }
            if (fragments.size != displays.size || isChange) {
                fragments.clear()
                displays.asSequence().map { display ->
                    fragments.firstOrNull { (it as TimeLineFragment).display.id == display.id }
                            ?: createFragment(display)
                }
            } else {
                null
            }
        } error {
            it.printStackTrace()
        } success {
            if (it != null) {
                previousFragmentIds.clear()
                previousFragmentIds.addAll(fragments.map { it.id })
                fragments.addAll(it)
                notifyDataSetChanged()
            } else {
            }
        }
    }


    private fun createFragments(): List<TimeLineFragment>
            = tweetsDisplayService.getDisplays(group).map { createFragment(it) }

    private fun createFragment(display: TweetsDisplay): TimeLineFragment {
        val clients = oAuthService.clients()
        fun TweetsDisplay.getClient(): TwitterClient = clients.find { it.id == this.token.id }!!
        fun TweetsDisplay.getUser(): TwitterUser = getClient().user()
        fun TweetsDisplay.getUserList() = getClient().twitter.showUserList(foreignId)
        fun TweetsDisplay.showUser() = TwitterUserCache.get(foreignId) ?: getClient().twitter.showUser(foreignId).convertAndCacheOrGet()
        return when (display.type) {
            TweetsDisplayType.HOME -> TimeLineFragment.create(display, "Home:${display.getUser().screenName}")
            TweetsDisplayType.MENTIONS -> TimeLineFragment.create(display, "Mentions:${display.getUser().screenName}")
            TweetsDisplayType.USER_LIST -> TimeLineFragment.create(display, "List${display.getUserList().name}")
            TweetsDisplayType.PUBLIC -> TimeLineFragment.create(display, "User:${display.showUser().screenName}")
        }
    }
}