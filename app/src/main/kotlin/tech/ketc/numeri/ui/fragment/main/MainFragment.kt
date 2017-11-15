package tech.ketc.numeri.ui.fragment.main

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.ui.components.IScrollableTabPagerComponent
import tech.ketc.numeri.ui.components.ScrollableTabPagerComponent
import tech.ketc.numeri.ui.fragment.timeline.TimelineFragment
import tech.ketc.numeri.ui.model.MainViewModel
import tech.ketc.numeri.ui.view.pager.ModifiablePagerAdapter
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.act
import tech.ketc.numeri.util.android.arg
import tech.ketc.numeri.util.arch.viewmodel.commonViewModel
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject

class MainFragment : Fragment(), AutoInject, TabLayout.OnTabSelectedListener,
        IScrollableTabPagerComponent by ScrollableTabPagerComponent() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: MainViewModel by commonViewModel { viewModelFactory }

    private val pagerAdapter by lazy { ModifiablePagerAdapter<String, TimelineFragment>(childFragmentManager) }
    private val groupName by lazy { arg.getString(EXTRA_TIMELINE_GROUP) }

    companion object {
        private val EXTRA_TIMELINE_GROUP = "EXTRA_TIMELINE_GROUP"
        fun create(groupName: String): MainFragment {
            return MainFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_TIMELINE_GROUP, groupName)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return createView(act)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.v(javaClass.name, "onViewCreated() restore:${savedInstanceState != null}")
        tab.setupWithViewPager(pager)
        pager.adapter = pagerAdapter
        tab.addOnTabSelectedListener(this)
        if (savedInstanceState == null)
            model.clients.observe(this) {
                it.ifPresent {
                    model.getClientUsers(this, it) {
                        it.ifPresent { initializeTimeline(it) }
                    }
                }
                it.ifError { it.printStackTrace() }
            }
    }

    private fun initializeTimeline(clientUsers: List<Pair<TwitterClient, TwitterUser>>) {
        fun setTimeline(infoList: List<TimelineInfo>) {
            model.createNameList(this, clientUsers, infoList) { names ->
                val contents = infoList.mapIndexed { i, info ->
                    ModifiablePagerAdapter.Content("${info.type.name}_${info.id}",
                            TimelineFragment.create(info), names[i])
                }
                pagerAdapter.setContents(contents)
            }
        }
        model.loadTimelineInfoList(this, groupName) {
            setTimeline(it)
        }
    }

    //interface impl
    override fun onTabReselected(tab: TabLayout.Tab) {
        pagerAdapter.getContent(tab.position).fragment.scrollToTop()
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        //do nothing
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        //do nothing
    }
}