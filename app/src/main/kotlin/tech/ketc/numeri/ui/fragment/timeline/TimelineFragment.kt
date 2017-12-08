package tech.ketc.numeri.ui.fragment.timeline

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.*
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.ui.activity.main.OnClickTweetFabListener
import tech.ketc.numeri.ui.activity.tweet.TweetActivity
import tech.ketc.numeri.ui.components.ISwipeRefreshRecyclerUIComponent
import tech.ketc.numeri.ui.components.SwipeRefreshRecyclerUIComponent
import tech.ketc.numeri.ui.fragment.operation.OperationTweetDialogFragment
import tech.ketc.numeri.ui.model.TimeLineViewModel
import tech.ketc.numeri.ui.model.delegate.HasTweetOperator
import tech.ketc.numeri.ui.model.delegate.ITweetOperator
import tech.ketc.numeri.ui.view.recycler.timeline.TimeLineDataSourceAdapter
import tech.ketc.numeri.ui.view.recycler.timeline.TweetViewHolder
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.Updatable
import tech.ketc.numeri.util.android.arg
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject
import tech.ketc.numeri.util.android.act
import tech.ketc.numeri.util.android.pref
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.logTag
import tech.ketc.numeri.util.twitter4j.showTwitterError
import java.io.Serializable

class TimelineFragment : Fragment(), AutoInject, OnClickTweetFabListener,
        ISwipeRefreshRecyclerUIComponent by SwipeRefreshRecyclerUIComponent(),
        HasTweetOperator, Updatable {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: TimeLineViewModel by viewModel { mViewModelFactory }

    private val mInfo by lazy { arg.getSerializable(EXTRA_INFO) as Info }
    private val mTlInfo: TimelineInfo
        get() = mInfo.timelineInfo
    private val mSwipeRefreshEnabled: Boolean
        get() = mInfo.swipeRefreshEnabled

    private var mSavedTopChildAdapterPosition: Int? = null
    private var mAdapter: TimeLineDataSourceAdapter? = null
    private val mPref by lazy { act.pref }
    private var mIsStreamEnabled = true
    private var mIsEnableAutoScroll = true
    private var mIsStreamStart = false
    private var mIsInitialized = false
    private lateinit var mClient: TwitterClient
    override val operator: ITweetOperator
        get() = mModel

    companion object {
        private val EXTRA_INFO = "EXTRA_TIMELINE_INFO"
        private val DEFAULT_PAGE_SIZE = 30
        private val EXTRA_RECYCLER_TOP_CHILD_ADAPTER_POSITION = "RECYCLER_TOP_CHILD_ADAPTER_POSITION"
        private val TAG_OPERATION_TWEET = "TAG_OPERATE_TWEET"
        fun create(timelineInfo: TimelineInfo, swipeRefreshEnabled: Boolean = true): TimelineFragment = TimelineFragment().apply {
            arguments = Bundle().apply {
                val info = Info(timelineInfo, swipeRefreshEnabled)
                putSerializable(EXTRA_INFO, info)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = createView(ctx)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let { restoreInstanceState(it) }
        Logger.v(javaClass.name, "onViewCreated() restore:${savedInstanceState != null}")
        loadPrefSetting()
        bindLaunch {
            val res = mModel.initialize(mTlInfo).await()
            val client = res.orError {
                toast(R.string.message_failed_acquire_info_necessary_for_browsing_timeline)
            } ?: return@bindLaunch
            initialize(client)
        }
    }

    private fun initialize(client: TwitterClient) {
        mClient = client
        fun create() = TweetViewHolder(ctx, client, this, mModel, this::onTweetItemClick)
        val adapter = TimeLineDataSourceAdapter(this, mModel.dataSource, ::create)
        mAdapter = adapter
        adapter.pageSize = DEFAULT_PAGE_SIZE
        recycler.adapter = adapter
        adapter.setStoreLiveData(mModel.storeTweetsLiveData)
        adapter.error = {
            Logger.printStackTrace(logTag, it)
            if (!showTwitterError(it))
                toast(R.string.message_failure_acquire_tweet)
        }
        if (adapter.restore()) {
            Logger.v(javaClass.name, "restore adapter")
            mSavedTopChildAdapterPosition?.let {
                recycler.scrollToPosition(it)
            }
            startStream()
            mIsInitialized = true
        } else {
            Logger.v(javaClass.name, "simpleInit adapter")
            initializeAdapter()
        }
        swipeRefresh.setOnRefreshListener { update() }
        mModel.deleteObserve(this) {
            adapter.delete(it)
        }
    }

    private fun initializeAdapter() {
        val adapter = mAdapter ?: throw IllegalStateException()
        swipeRefresh.isEnabled = false
        swipeRefresh.isRefreshing = true
        adapter.loadInitial {
            swipeRefresh.isRefreshing = false
            swipeRefresh.isEnabled = true
            startStream()
            mIsInitialized = true
        }
    }

    private fun startStream() {
        mIsStreamStart = mModel.startStream(this) {
            if (mIsStreamEnabled) {
                mAdapter!!.insertTop(it)
                if (mIsEnableAutoScroll) autoScroll()
            } else {
                mAdapter!!.store(it)
            }
        }
        setSwipeRefreshEnabled()
    }

    private fun setSwipeRefreshEnabled() {
        if (mIsStreamStart) {
            swipeRefresh.isEnabled = !mIsStreamEnabled
            Logger.v(logTag, "swipeRefresh isEnabled:${!mIsStreamEnabled}")
        } else {
            swipeRefresh.isEnabled = mSwipeRefreshEnabled
            Logger.v(logTag, "swipeRefresh isEnabled:$mSwipeRefreshEnabled")
        }
    }

    override fun onResume() {
        super.onResume()
        loadPrefSetting()
        setSwipeRefreshEnabled()
    }

    private fun loadPrefSetting() {
        val prevStreamEnabled = mIsStreamEnabled
        mIsStreamEnabled = mPref.getBoolean(getString(R.string.pref_key_is_stream_enabled), true)
        mIsEnableAutoScroll = mPref.getBoolean(getString(R.string.pref_key_auto_scroll), true)
        if (mIsStreamStart && prevStreamEnabled != mIsStreamEnabled) {
            changeStreamSetting()
        }
        Logger.v(logTag, "isStreamEnabled $mIsStreamEnabled")
    }

    private fun changeStreamSetting() {
        if (mIsStreamEnabled) {
            mAdapter!!.marge()
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(EXTRA_RECYCLER_TOP_CHILD_ADAPTER_POSITION, getChildAdapterPosition() ?: -1)
        super.onSaveInstanceState(outState)
    }

    private fun getChildAdapterPosition(): Int? {
        val adapter = mAdapter
        val topChild = recycler.getChildAt(0)
        return if (adapter != null && topChild != null) {
            val childAt = recycler.getChildAt(0)
            recycler.getChildAdapterPosition(childAt)
        } else null
    }

    private fun onTweetItemClick(tweet: Tweet) {
        OperationTweetDialogFragment.create(mClient, tweet).show(childFragmentManager, TAG_OPERATION_TWEET)
    }


    private fun autoScroll() {
        getChildAdapterPosition()?.let { if (it == 1) scrollToTop() }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        mSavedTopChildAdapterPosition = savedInstanceState.getInt(EXTRA_RECYCLER_TOP_CHILD_ADAPTER_POSITION)
                .takeIf { it != -1 }
    }

    fun scrollToTop() = recycler.scrollToPosition(0)

    override fun update(complete: () -> Unit) {
        if (!mIsInitialized) return complete()
        val adapter = mAdapter ?: return complete()
        swipeRefresh.isRefreshing = true
        adapter.loadAfter {
            swipeRefresh.isRefreshing = false
            complete()
        }
    }

    override fun onClickTweetFab() {
        if (!mIsInitialized) return
        TweetActivity.start(ctx, client = mClient)
    }

    data class Info(val timelineInfo: TimelineInfo, val swipeRefreshEnabled: Boolean) : Serializable
}