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
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.ui.components.ISwipeRefreshRecyclerComponent
import tech.ketc.numeri.ui.components.SwipeRefreshRecyclerComponent
import tech.ketc.numeri.ui.model.TimeLineViewModel
import tech.ketc.numeri.ui.view.recycler.timeline.TimeLineDataSourceAdapter
import tech.ketc.numeri.ui.view.recycler.timeline.TweetViewHolder
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.arg
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject
import tech.ketc.numeri.util.android.act
import tech.ketc.numeri.util.android.pref
import tech.ketc.numeri.util.logTag

class TimelineFragment : Fragment(), AutoInject, ISwipeRefreshRecyclerComponent by SwipeRefreshRecyclerComponent() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: TimeLineViewModel by viewModel { viewModelFactory }

    private val tlInfo by lazy { arg.getSerializable(EXTRA_TIMELINE_INFO) as TimelineInfo }
    private var savedTopChildAdapterPosition: Int? = null
    private var mAdapter: TimeLineDataSourceAdapter? = null
    private val pref by lazy { act.pref }
    private var mIsStreamEnabled = true
    private var mIsEnableAutoScroll = true
    private var mIsStreamStart = false

    companion object {
        private val EXTRA_TIMELINE_INFO = "EXTRA_TIMELINE_INFO"
        private val DEFAULT_PAGE_SIZE = 30
        private val EXTRA_RECYCLER_TOP_CHILD_ADAPTER_POSITION = "RECYCLER_TOP_CHILD_ADAPTER_POSITION"
        fun create(info: TimelineInfo): TimelineFragment = TimelineFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_TIMELINE_INFO, info)
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
        model.initialize(tlInfo, this, callback = {
            initialize(it)
        }, error = {
            toast(R.string.message_failed_acquire_info_necessary_for_browsing_timeline)
        })
    }

    private fun initialize(client: TwitterClient) {
        val adapter = TimeLineDataSourceAdapter(this,
                model.dataSource, { TweetViewHolder(ctx, client, this, model) })
        mAdapter = adapter
        adapter.pageSize = DEFAULT_PAGE_SIZE
        recycler.adapter = adapter
        adapter.setStoreLiveData(model.storeTweetsLiveData)
        adapter.error = {
            toast(R.string.message_failure_acquire_tweet)
        }
        if (adapter.restore()) {
            Logger.v(javaClass.name, "restore adapter")
            savedTopChildAdapterPosition?.let {
                recycler.scrollToPosition(it)
            }
            startStream()
        } else {
            Logger.v(javaClass.name, "initialize adapter")
            initializeAdapter()
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
        }
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            adapter.loadAfter {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun startStream() {
        mIsStreamStart = model.startStream(this) {
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
            swipeRefresh.isEnabled = false
            Logger.v(logTag, "swipeRefresh isEnabled:false")
        }
    }

    override fun onResume() {
        super.onResume()
        loadPrefSetting()
        setSwipeRefreshEnabled()
    }

    private fun loadPrefSetting() {
        val prevStreamEnabled = mIsStreamEnabled
        mIsStreamEnabled = pref.getBoolean(getString(R.string.pref_key_is_stream_enabled), true)
        mIsEnableAutoScroll = pref.getBoolean(getString(R.string.pref_key_auto_scroll), true)
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
        getChildAdapterPosition()?.let {
            outState.putInt(EXTRA_RECYCLER_TOP_CHILD_ADAPTER_POSITION, it)
        }
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


    private fun autoScroll() {
        getChildAdapterPosition()?.let { if (it == 1) scrollToTop() }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        savedTopChildAdapterPosition = savedInstanceState.getInt(EXTRA_RECYCLER_TOP_CHILD_ADAPTER_POSITION)
    }

    fun scrollToTop() = recycler.scrollToPosition(0)
}