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
import net.ketc.numeri.presentation.view.component.TweetViewHolder
import net.ketc.numeri.presentation.view.component.adapter.TwitterRecyclerAdapter
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
        get() = twitterAdapter.last

    override val firstTweet: Tweet?
        get() = twitterAdapter.first

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

    override var isReadMorEnabled: Boolean
        get() = twitterAdapter.isReadMoreEnable
        set(value) {
            twitterAdapter.isReadMoreEnable = value
        }

    override val displayName: String by lazy { display.name }

    private var mTwitterAdapter: TwitterRecyclerAdapter<Tweet>? = null
    private val twitterAdapter: TwitterRecyclerAdapter<Tweet>
        get() = mTwitterAdapter ?: throw IllegalStateException("TwitterClient is not set")

    private val swipeRefresh: SwipeRefreshLayout by lazy {
        view!!.find<SwipeRefreshLayout>(R.id.swipe_refresh)
    }

    private val tweetsRecycler: RecyclerView by lazy {
        view!!.find<RecyclerView>(R.id.tweets_recycler)
    }

    init {
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = createView(context)
        view.find<RecyclerView>(R.id.tweets_recycler).defaultInit()
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

    override fun addAll(tweets: List<Tweet>) = twitterAdapter.addAll(tweets)
    override fun insertAllToTop(tweets: List<Tweet>) = twitterAdapter.insertAllToTop(tweets)
    override fun remove(tweet: Tweet) = twitterAdapter.remove(tweet)
    override fun remove(id: Long) = twitterAdapter.remove(id)
    override fun insert(tweet: Tweet) = twitterAdapter.insertTop(tweet)
    override fun showDialog(dialog: AppCompatDialog) = dialogOwner.showDialog(dialog)
    override fun setClient(client: TwitterClient) {
        mTwitterAdapter = TwitterRecyclerAdapter(presenter, {
            TweetViewHolder(parent, presenter, client, { presenter.onClickTweet(it) })
        }, presenter)
        tweetsRecycler.adapter = twitterAdapter
    }

    override fun scrollToTop() {
        if (twitterAdapter.first != null) {
            tweetsRecycler.scrollToPosition(0)
        }
    }

    companion object {
        val EXTRA_DISPLAY = "EXTRA_DISPLAY"

        fun create(display: TweetsDisplay) = TimeLineFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_DISPLAY, display)
            }
        }

        private fun createView(ctx: Context) = ctx.relativeLayout {
            lparams(matchParent, matchParent)
            swipeRefreshLayout {
                lparams(matchParent, matchParent)
                id = R.id.swipe_refresh
                isEnabled = false
                recyclerView {
                    id = R.id.tweets_recycler
                    isVerticalScrollBarEnabled = true
                }.lparams(matchParent, matchParent)
            }
        }

    }

}

interface TimeLineFragmentInterface : FragmentInterface {
    val display: TweetsDisplay
    val lastTweet: Tweet?
    val firstTweet: Tweet?
    var isRefreshing: Boolean
    var isRefreshable: Boolean
    var isReadMorEnabled: Boolean
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
