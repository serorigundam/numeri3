package tech.ketc.numeri.ui.fragment.search

import android.arch.lifecycle.ViewModelProvider
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.support.v4.ctx
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.ui.components.ISwipeRefreshRecyclerUIComponent
import tech.ketc.numeri.ui.components.SwipeRefreshRecyclerUIComponent
import tech.ketc.numeri.ui.fragment.operation.OperationTweetDialogFragment
import tech.ketc.numeri.ui.model.SearchViewModel
import tech.ketc.numeri.ui.model.delegate.HasTweetOperator
import tech.ketc.numeri.ui.model.delegate.ITweetOperator
import tech.ketc.numeri.ui.view.recycler.timeline.TimeLineDataSourceAdapter
import tech.ketc.numeri.ui.view.recycler.timeline.TweetViewHolder
import tech.ketc.numeri.util.Updatable
import tech.ketc.numeri.util.android.arg
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import tech.ketc.numeri.util.twitter4j.showTwitterError
import java.io.Serializable
import javax.inject.Inject

class SearchFragment : Fragment(), AutoInject, Updatable, HasTweetOperator,
        ISwipeRefreshRecyclerUIComponent by SwipeRefreshRecyclerUIComponent() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: SearchViewModel by viewModel { mViewModelFactory }

    private val mInfo by lazy { arg.getSerializable(EXTRA_INFO) as Info }
    private val mClient: TwitterClient
        get() = mInfo.client
    private val mQuery: String
        get() = mInfo.query
    private val mIsSwipeRefreshEnabled: Boolean
        get() = mInfo.isSwipeRefreshEnabled

    private var mInitialized: Boolean = false
    private var mSavedPosition: Int? = null

    private val mAdapter by lazy { TimeLineDataSourceAdapter(this, mModel.dataSource, ::createTweetViewHolder) }

    companion object {
        private val EXTRA_INFO = "EXTRA_INFO"
        private val SAVED_POSITION = "SAVED_POSITION"
        private val TAG_OPERATION = "TAG_OPERATION"
        private val MAX = 50//api max 100
        fun create(client: TwitterClient, query: String,
                   isSwipeRefreshEnabled: Boolean) = SearchFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_INFO, Info(client, query, isSwipeRefreshEnabled))
            }
        }

    }

    override val operator: ITweetOperator
        get() = mModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = createView(ctx)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshing()
        savedInstanceState?.run(this::restore)
        if (savedInstanceState == null)
            mModel.initialize(mClient, mQuery)
        mAdapter.setStoreLiveData(mModel.storeLiveData)
        recycler.adapter = mAdapter
        swipeRefresh.isRefreshing = true
        mAdapter.error = {
            showTwitterError(it)
        }
        mAdapter.pageSize = MAX

        if (mAdapter.restore()) {
            mSavedPosition?.let { recycler.scrollToPosition(it) }
            unRefreshing()
            mInitialized = true
        } else {
            mAdapter.loadInitial {
                unRefreshing()
                mInitialized = true
            }
        }
        swipeRefresh.setOnRefreshListener {
            refreshing()
            update()
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        val position = mAdapter.itemCount.takeIf { it > 0 }?.let {
            recycler.getChildAdapterPosition(recycler.getChildAt(0))
        }
        outState.putInt(SAVED_POSITION, position ?: -1)
        super.onSaveInstanceState(outState)
    }

    private fun restore(savedInstanceState: Bundle) {
        savedInstanceState.getInt(SAVED_POSITION).takeIf { it != -1 }?.let {
            mSavedPosition = it
        }
    }

    private fun unRefreshing() {
        swipeRefresh.isEnabled = mIsSwipeRefreshEnabled
        swipeRefresh.isRefreshing = false
    }

    private fun refreshing() {
        swipeRefresh.isEnabled = false
        swipeRefresh.isRefreshing = true
    }


    private fun createTweetViewHolder() = TweetViewHolder(ctx, mClient, this, mModel, this::onClickTweetItem)


    private fun onClickTweetItem(tweet: Tweet) {
        OperationTweetDialogFragment.create(mClient, tweet)
                .show(childFragmentManager, TAG_OPERATION)
    }

    override fun update(complete: () -> Unit) {
        if (!mInitialized) return complete()
        refreshing()
        mAdapter.loadAfter {
            unRefreshing()
            complete()
        }
    }

    fun scrollToTop() {
        if (!mInitialized) return
        mAdapter.itemCount.takeIf { it > 0 }?.let {
            recycler.scrollToPosition(0)
        }
    }

    data class Info(val client: TwitterClient, val query: String, val isSwipeRefreshEnabled: Boolean) : Serializable
}