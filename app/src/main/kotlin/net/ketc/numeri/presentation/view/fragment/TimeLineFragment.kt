package net.ketc.numeri.presentation.view.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.ketc.numeri.R
import net.ketc.numeri.domain.entity.*
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.fragment.tweet.display.*
import net.ketc.numeri.presentation.view.Refreshable
import net.ketc.numeri.presentation.view.SimplePagerContent
import net.ketc.numeri.presentation.view.component.TweetViewHolder
import net.ketc.numeri.presentation.view.component.adapter.ReadableMoreRecyclerAdapter
import net.ketc.numeri.presentation.view.component.adapter.remove
import net.ketc.numeri.presentation.view.component.ui.TweetViewUI
import net.ketc.numeri.util.android.DialogOwner
import net.ketc.numeri.util.android.defaultInit
import net.ketc.numeri.util.android.parent
import org.jetbrains.anko.find
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class TimeLineFragment : ApplicationFragment<TimeLinePresenter>(), TimeLineFragmentInterface {
    override val presenter: TimeLinePresenter by lazy { createTimeLinePresenter(display) }
    private val dialogOwner = DialogOwner()

    override val activity: AppCompatActivity
        get() = this.parent

    override val display: TweetsDisplay by lazy {
        arguments.getSerializable(EXTRA_DISPLAY) as TweetsDisplay
    }

    override val lastTweet: Tweet?
        get() = readableMoreAdapter.last

    override val firstTweet: Tweet?
        get() = readableMoreAdapter.first

    override var isRefreshing: Boolean
        get() = swipeRefresh.isRefreshing
        set(value) {
            swipeRefresh.isRefreshing = value
        }

    override var isRefreshable: Boolean
        get() = swipeRefresh.isEnabled
        set(value) {
            swipeRefresh.isEnabled = value
        }

    override var isReadMoreEnabled: Boolean
        get() = readableMoreAdapter.isReadMoreEnabled
        set(value) {
            readableMoreAdapter.isReadMoreEnabled = value
        }

    override var isEmptyFooterEnabled: Boolean
        get() = readableMoreAdapter.isEmptyFooterEnabled
        set(value) {
            readableMoreAdapter.isEmptyFooterEnabled = value
        }

    override val refreshableConfig: Boolean by lazy { arguments.getBoolean(EXTRA_REFRESHABLE) }

    override val displayName: String by lazy { display.name }

    override val contentName: String
        get() = displayName

    private var mReadableMoreAdapter: ReadableMoreRecyclerAdapter<Tweet>? = null
    private val readableMoreAdapter: ReadableMoreRecyclerAdapter<Tweet>
        get() = mReadableMoreAdapter ?: throw IllegalStateException("TwitterClient is not set")

    private val swipeRefresh: SwipeRefreshLayout by lazy {
        view!!.find<SwipeRefreshLayout>(R.id.swipe_refresh)
    }

    private val tweetsRecycler: RecyclerView by lazy {
        view!!.find<RecyclerView>(R.id.tweet_recycler)
    }

    init {
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = createView(context)
        view.find<RecyclerView>(R.id.tweet_recycler).defaultInit()
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        swipeRefresh.setOnRefreshListener { presenter.update() }
        presenter.initialize()
    }

    private fun createTimeLinePresenter(timeLineDisplay: TweetsDisplay): TimeLinePresenter {
        return when (timeLineDisplay.type) {
            TweetsDisplayType.HOME -> HomePresenter(this)
            TweetsDisplayType.MENTIONS -> MentionsPresenter(this)
            TweetsDisplayType.USER_LIST -> UserListPresenter(this)
            TweetsDisplayType.PUBLIC -> PublicTimeLinePresenter(this)
            TweetsDisplayType.FAVORITE -> FavoritePresenter(this)
            TweetsDisplayType.MEDIA -> MediaTimeLinePresenter(this)
            else -> throw InternalError()
        }
    }

    override fun onPause() {
        super.onPause()
        dialogOwner.onPause()
    }

    override fun onResume() {
        super.onResume()
        dialogOwner.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialogOwner.onDestroy()
    }

    override fun addAll(tweets: List<Tweet>) = readableMoreAdapter.addAll(tweets)
    override fun insertAllToTop(tweets: List<Tweet>) = readableMoreAdapter.insertAllToTop(tweets)
    override fun remove(tweet: Tweet) = readableMoreAdapter.remove(tweet)
    override fun remove(id: Long) = readableMoreAdapter.remove(id)
    override fun insert(tweet: Tweet) = readableMoreAdapter.insertTop(tweet)
    override fun showDialog(dialog: AppCompatDialog) = dialogOwner.showDialog(dialog)
    override fun setClient(client: TwitterClient) {
        mReadableMoreAdapter = ReadableMoreRecyclerAdapter(presenter, {
            TweetViewHolder(TweetViewUI(parent), presenter, client, { presenter.onClickTweet(it) })
        }, presenter)
        tweetsRecycler.adapter = readableMoreAdapter
    }

    override fun scrollToTop() {
        if (readableMoreAdapter.first != null) {
            tweetsRecycler.scrollToPosition(0)
        }
    }

    override fun refresh(callback: () -> Unit) {
        if (!isRefreshing) {
            presenter.update(callback)
        } else {
            callback()
        }
    }

    companion object {
        val EXTRA_DISPLAY = "EXTRA_DISPLAY"
        val EXTRA_REFRESHABLE = "EXTRA_REFRESHABLE"
        fun create(display: TweetsDisplay, refreshable: Boolean = true) = TimeLineFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_DISPLAY, display)
                putBoolean(EXTRA_REFRESHABLE, refreshable)
            }
        }

        private fun createView(ctx: Context) = ctx.relativeLayout {
            lparams(matchParent, matchParent)
            swipeRefreshLayout {
                lparams(matchParent, matchParent)
                id = R.id.swipe_refresh
                recyclerView {
                    id = R.id.tweet_recycler
                    isVerticalScrollBarEnabled = true
                }.lparams(matchParent, matchParent)
            }
        }

    }

}

interface TimeLineFragmentInterface : FragmentInterface, Refreshable, SimplePagerContent {
    val display: TweetsDisplay
    val lastTweet: Tweet?
    val firstTweet: Tweet?
    var isRefreshing: Boolean
    var isRefreshable: Boolean
    var isReadMoreEnabled: Boolean
    var isEmptyFooterEnabled: Boolean
    val refreshableConfig: Boolean
    val displayName: String
    fun setClient(client: TwitterClient)
    fun addAll(tweets: List<Tweet>)
    fun insertAllToTop(tweets: List<Tweet>)
    fun remove(tweet: Tweet)
    fun remove(id: Long)
    fun insert(tweet: Tweet)
    fun showDialog(dialog: AppCompatDialog)
    fun scrollToTop()
}
