package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.os.Bundle
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.activity.ConversationPresenter
import net.ketc.numeri.presentation.presenter.activity.ConversationPresenterFactory
import net.ketc.numeri.presentation.view.activity.ui.ConversationActivityUI
import net.ketc.numeri.presentation.view.activity.ui.IConversationActivityUI
import net.ketc.numeri.presentation.view.component.TweetViewHolder
import net.ketc.numeri.presentation.view.component.adapter.ReadableMoreRecyclerAdapter
import net.ketc.numeri.presentation.view.component.ui.TweetViewUI
import net.ketc.numeri.presentation.view.fragment.dialog.TweetOperateDialogFragment
import net.ketc.numeri.util.android.defaultInit
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity

class ConversationActivity : ApplicationActivity<ConversationPresenter>(), ConversationActivityInterface,
        IConversationActivityUI by ConversationActivityUI() {
    override val presenterFactory = ConversationPresenterFactory

    override val ctx: Context = this
    override val statusId: Long by lazy { intent.getLongExtra(EXTRA_STATUS_ID, -1) }
    override val client: TwitterClient by lazy { (intent.getSerializableExtra(EXTRA_CLIENT) as? TwitterClient) ?: throw IllegalStateException() }
    private val adapter by lazy {
        ReadableMoreRecyclerAdapter(presenter, {
            TweetViewHolder(TweetViewUI(this), presenter, client) {
                TweetOperateDialogFragment.create(it, client).show(supportFragmentManager, tag)
            }
        })
    }
    override var visibleTopPosition: Int
        get() = tweetsRecycler.findViewHolderForLayoutPosition(0)?.adapterPosition ?: -1
        set(value) {
            tweetsRecycler.scrollToPosition(value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        presenter.activity = this
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        tweetsRecycler.defaultInit()
        tweetsRecycler.adapter = adapter
        presenter.initialize(savedInstanceState, isStartedForFirst)
    }

    override fun insert(tweet: Tweet) {
        adapter.insertTop(tweet)
        tweetsRecycler.scrollToPosition(0)
    }

    override fun insertAll(tweets: List<Tweet>) {
        adapter.insertAllToTop(tweets)
        tweetsRecycler.scrollToPosition(0)
    }

    companion object {
        fun start(ctx: Context, statusId: Long, client: TwitterClient) {
            ctx.startActivity<ConversationActivity>(EXTRA_STATUS_ID to statusId,
                    EXTRA_CLIENT to client)
        }

        private val EXTRA_STATUS_ID = "EXTRA_STATUS_ID"
        private val EXTRA_CLIENT = "EXTRA_CLIENT"
    }
}

interface ConversationActivityInterface : ActivityInterface {
    val statusId: Long
    val client: TwitterClient
    var visibleTopPosition: Int
    fun insert(tweet: Tweet)
    fun insertAll(tweets: List<Tweet>)
}