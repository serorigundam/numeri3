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

class TimelineFragment : Fragment(), AutoInject, ISwipeRefreshRecyclerComponent by SwipeRefreshRecyclerComponent() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: TimeLineViewModel by viewModel { viewModelFactory }

    private val tlInfo by lazy { arg.getSerializable(EXTRA_TIMELINE_INFO) as TimelineInfo }
    private var savedTopChildAdapterPosition: Int? = null
    private var mAdapter: TimeLineDataSourceAdapter? = null

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
        }
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            adapter.loadAfter {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val adapter = mAdapter
        val topChild = recycler.getChildAt(0)
        if (adapter != null && topChild != null) {
            val childAt = recycler.getChildAt(0)
            val childAdapterPosition = recycler.getChildAdapterPosition(childAt)
            outState.putInt(EXTRA_RECYCLER_TOP_CHILD_ADAPTER_POSITION, childAdapterPosition)
        }
        super.onSaveInstanceState(outState)
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        savedTopChildAdapterPosition = savedInstanceState.getInt(EXTRA_RECYCLER_TOP_CHILD_ADAPTER_POSITION)
    }

    fun scrollToTop() = recycler.scrollToPosition(0)
}