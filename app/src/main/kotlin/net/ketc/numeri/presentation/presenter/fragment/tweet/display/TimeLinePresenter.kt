package net.ketc.numeri.presentation.presenter.fragment.tweet.display

import android.os.Handler
import net.ketc.numeri.R
import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.fragment.AutoDisposableFragmentPresenter
import net.ketc.numeri.presentation.view.component.ReadableMore
import net.ketc.numeri.presentation.view.component.TweetOperatorDialogFactory
import net.ketc.numeri.presentation.view.fragment.TimeLineFragmentInterface
import net.ketc.numeri.util.rx.MySchedulers
import org.jetbrains.anko.toast
import twitter4j.Paging
import javax.inject.Inject

abstract class TimeLinePresenter(timeLineFragment: TimeLineFragmentInterface) : AutoDisposableFragmentPresenter<TimeLineFragmentInterface>(), ReadableMore<MutableList<Tweet>> {
    @Inject
    lateinit var oAuthService: OAuthService

    override val fragment: TimeLineFragmentInterface = timeLineFragment
    private lateinit var mClient: TwitterClient
    protected val client: TwitterClient
        get() = mClient

    private val handler = Handler()
    private val unlockUpdate: () -> Unit = { fragment.isRefreshable = true }

    init {
        inject()
    }

    abstract fun getTweets(paging: Paging): List<Tweet>

    override fun initialize() {
        singleTask(MySchedulers.twitter) {
            oAuthService.clients()
        } success {
            mClient = it.first { it.id == fragment.display.token.id }
            fragment.setClient(client)
            client.stream.onDeletionNoticeFlowable.subscribe {
                fragment.remove(it.statusId)
            }.autoDispose()
            beforeInitializeLoad()
            initializeLoad()
            afterInitializeLoad()
        }
    }

    private fun initializeLoad() {
        fragment.isRefreshing = true
        singleTask(MySchedulers.twitter) {
            getTweets(Paging().apply {
                count = DEFAULT_COUNT
            })
        } error {
            it.printStackTrace()
            fragment.activity.toast("error")
            fragment.isReadMorEnabled = true
            fragment.isRefreshing = false
        } success {
            fragment.addAll(it)
            fragment.isReadMorEnabled = true
            fragment.isRefreshing = false
        }
    }

    /**
     * callback at before initialization load
     */
    abstract fun beforeInitializeLoad()

    /**
     * callback at initialization load completion
     */
    abstract fun afterInitializeLoad()

    fun update() {
        fragment.isRefreshing = true
        singleTask(MySchedulers.twitter) {
            getTweets(Paging().apply {
                count = DEFAULT_COUNT
                fragment.firstTweet?.let {
                    sinceId = it.id
                }
            })
        } error {
            it.printStackTrace()
            fragment.isRefreshing = false
            fragment.activity.toast("error")
            lockUpdate()
        } success {
            fragment.isRefreshing = false
            fragment.insertAllToTop(it)
            lockUpdate()
        }
    }

    private fun lockUpdate() {
        fragment.activity.run { toast(getString(R.string.lock_tweet_cquisition)) }
        fragment.isRefreshable = false
        handler.postDelayed(unlockUpdate, UPDATE_LOCK_MILLS)
    }

    fun onClickTweet(tweet: Tweet) {
        val dialog = TweetOperatorDialogFactory(fragment.activity, tweet.retweetedTweet ?: tweet, this) {
            it.printStackTrace()
            fragment.activity.toast("error")
        }.create(client)
        fragment.showDialog(dialog)
    }

    override fun read(): MutableList<Tweet> {
        val paging = Paging().apply {
            count = DEFAULT_COUNT
            fragment.lastTweet?.let { maxId = it.id }
        }
        return getTweets(paging).toMutableList()
    }


    override fun error(throwable: Throwable) {
        throwable.printStackTrace()
        fragment.activity.toast("error")
    }

    override fun complete(t: MutableList<Tweet>) {
        val empty = fragment.firstTweet == null
        if (!t.isEmpty() && !empty) {
            t.removeAt(0)
        }
        fragment.addAll(t)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(unlockUpdate)
    }

    companion object {
        val DEFAULT_COUNT = 40
        val UPDATE_LOCK_MILLS: Long = 30 * 1000
    }
}

