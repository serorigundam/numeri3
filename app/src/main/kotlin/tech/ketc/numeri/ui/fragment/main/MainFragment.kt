package tech.ketc.numeri.ui.fragment.main

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import tech.ketc.numeri.R
import tech.ketc.numeri.ui.activity.timelinemanage.TimelineManageActivity
import tech.ketc.numeri.ui.components.IScrollableTabPagerUIComponent
import tech.ketc.numeri.ui.components.ScrollableTabPagerUIComponent
import tech.ketc.numeri.ui.fragment.timeline.TimelineFragment
import tech.ketc.numeri.ui.model.MainViewModel
import tech.ketc.numeri.ui.view.pager.ModifiablePagerAdapter
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.act
import tech.ketc.numeri.util.android.arg
import tech.ketc.numeri.util.arch.lifecycle.IOnActiveRunner
import tech.ketc.numeri.util.arch.lifecycle.OnActiveRunner
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.nullable
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.arch.viewmodel.commonViewModel
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject

class MainFragment : Fragment(), AutoInject, TabLayout.OnTabSelectedListener,
        IScrollableTabPagerUIComponent by ScrollableTabPagerUIComponent(),
        IOnActiveRunner by OnActiveRunner() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: MainViewModel by commonViewModel { mViewModelFactory }

    private val mPagerAdapter by lazy { ModifiablePagerAdapter<String, TimelineFragment>(childFragmentManager) }
    private val mGroupName by lazy { arg.getString(EXTRA_TIMELINE_GROUP) }
    private var mInitialized = false

    companion object {
        private val EXTRA_TIMELINE_GROUP = "EXTRA_TIMELINE_GROUP"
        private val TAG_NO_CONTENT = "TAG_NO_CONTENT"
        private val SAVED_INITIALIZED = "SAVED_INITIALIZED"
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
        savedInstanceState?.let(this::restore)
        setOwner(this)
        Logger.v(javaClass.name, "onViewCreated() restore:${savedInstanceState != null}")
        tab.setupWithViewPager(pager)
        pager.adapter = mPagerAdapter
        tab.visibility = View.INVISIBLE
        tab.addOnTabSelectedListener(this)
        bindLaunch {
            if (!mInitialized) initializeTimeline()
            if (mPagerAdapter.count > 0)
                tab.visibility = View.VISIBLE
            mModel.timelineChange(this@MainFragment) {
                bindLaunch {
                    initializeTimeline()
                }
            }
        }
    }

    private suspend fun initializeTimeline() {
        val clientRes = mModel.clients().await()
        val clients = clientRes.orError {
            toast(R.string.message_failed_user_info)
        } ?: return
        val usersRes = mModel.getClientUsers(clients).await()
        val users = usersRes.orError {
            toast(R.string.message_failed_user_info)
        } ?: return
        val infoListRes = mModel.loadTimelineInfoList(mGroupName).await()
        val infoList = infoListRes.nullable() ?: return
        val namesRes = mModel.createNameList(users, infoList).await()
        val names = namesRes.nullable() ?: return
        val contents = infoList.mapIndexed { i, info ->
            ModifiablePagerAdapter.Content("${info.type.name}_${info.id}",
                    TimelineFragment.create(info), names[i])
        }
        runOnActive {
            mInitialized = true
            mPagerAdapter.setContents(contents)
            checkHasContent()
        }
    }

    private fun restore(savedInstanceState: Bundle) {
        mInitialized = savedInstanceState.getBoolean(SAVED_INITIALIZED)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SAVED_INITIALIZED, mInitialized)
        super.onSaveInstanceState(outState)
    }

    private fun showNoContentFragment() {
        val fragment = childFragmentManager.findFragmentByTag(TAG_NO_CONTENT)
                ?: NoContentFragment.create(mGroupName).also {
            childFragmentManager.beginTransaction().add(componentId, it, TAG_NO_CONTENT).commit()
        }
        childFragmentManager.beginTransaction().show(fragment).commit()
    }

    private fun hideNoContentFragment() {
        val fragment = childFragmentManager.findFragmentByTag(TAG_NO_CONTENT) ?: return
        childFragmentManager.beginTransaction().hide(fragment).commit()
    }

    private fun checkHasContent() {
        if (mPagerAdapter.count > 0) {
            tab.visibility = View.VISIBLE
            hideNoContentFragment()
        } else {
            tab.visibility = View.INVISIBLE
            showNoContentFragment()
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

    class NoContentFragment : Fragment() {
        private val mGroupName by lazy { arg.getString(EXTRA_GROUP_NAME) }

        companion object {
            private val EXTRA_GROUP_NAME = "EXTRA_GROUP_NAME"

            fun create(groupName: String) = NoContentFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_GROUP_NAME, groupName)
                }
            }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = ctx.relativeLayout {
            lparams(matchParent, matchParent)
            textView {
                val str = "add Timeline"
                text = SpannableString(str).apply {
                    setSpan(object : ClickableSpan() {
                        override fun onClick(p0: View?) {
                            TimelineManageActivity.start(act, mGroupName)
                        }
                    }, 0, str.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                }
                movementMethod = LinkMovementMethod.getInstance()
            }.lparams(wrapContent, wrapContent) {
                centerInParent()
            }
        }
    }
}