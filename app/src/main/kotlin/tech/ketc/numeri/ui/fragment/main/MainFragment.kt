package tech.ketc.numeri.ui.fragment.main

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.support.v4.toast
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.ui.components.IScrollableTabPagerUIComponent
import tech.ketc.numeri.ui.components.ScrollableTabPagerUIComponent
import tech.ketc.numeri.ui.fragment.timeline.TimelineFragment
import tech.ketc.numeri.ui.model.MainViewModel
import tech.ketc.numeri.ui.view.pager.ModifiablePagerAdapter
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.act
import tech.ketc.numeri.util.android.arg
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.nullable
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.arch.viewmodel.commonViewModel
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject

class MainFragment : Fragment(), AutoInject, TabLayout.OnTabSelectedListener,
        IScrollableTabPagerUIComponent by ScrollableTabPagerUIComponent() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: MainViewModel by commonViewModel { mViewModelFactory }

    private val mPagerAdapter by lazy { ModifiablePagerAdapter<String, TimelineFragment>(childFragmentManager) }
    private val mGroupName by lazy { arg.getString(EXTRA_TIMELINE_GROUP) }

    companion object {
        private val EXTRA_TIMELINE_GROUP = "EXTRA_TIMELINE_GROUP"
        fun create(groupName: String) = MainFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_TIMELINE_GROUP, groupName)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = createView(act)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.v(javaClass.name, "onViewCreated() restore:${savedInstanceState != null}")
        tab.setupWithViewPager(pager)
        pager.adapter = mPagerAdapter
        tab.addOnTabSelectedListener(this)
        bindLaunch {
            val clientRes = mModel.clients().await()
            val clients = clientRes.orError {
                toast(R.string.message_failed_user_info)
            } ?: return@bindLaunch
            val usersRes = mModel.getClientUsers(clients).await()
            val users = usersRes.orError {
                toast(R.string.message_failed_user_info)
            } ?: return@bindLaunch
            if (savedInstanceState == null) {
                val infoListRes = mModel.loadTimelineInfoList(mGroupName).await()
                val infoList = infoListRes.nullable() ?: return@bindLaunch
                val namesRes = mModel.createNameList(users, infoList).await()
                val names = namesRes.nullable() ?: return@bindLaunch
                val contents = infoList.mapIndexed { i, info ->
                    ModifiablePagerAdapter.Content("${info.type.name}_${info.id}",
                            TimelineFragment.create(info), names[i])
                }
                mPagerAdapter.setContents(contents)
            }
            mModel.timelineChange(this@MainFragment) {
                initializeTimeline(users)
            }
        }
    }

    private fun initializeTimeline(clientUsers: List<Pair<TwitterClient, TwitterUser>>) {
        bindLaunch {
            val infoListRes = mModel.loadTimelineInfoList(mGroupName).await()
            val infoList = infoListRes.nullable() ?: return@bindLaunch
            val namesRes = mModel.createNameList(clientUsers, infoList).await()
            val names = namesRes.nullable() ?: return@bindLaunch
            val contents = infoList.mapIndexed { i, info ->
                ModifiablePagerAdapter.Content("${info.type.name}_${info.id}",
                        TimelineFragment.create(info), names[i])
            }
            mPagerAdapter.setContents(contents)
        }
    }

    //interface impl
    override fun onTabReselected(tab: TabLayout.Tab) {
        mPagerAdapter.getContent(tab.position).fragment.scrollToTop()
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        //do nothing
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        //do nothing
    }
}