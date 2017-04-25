package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatDialog
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.activity.ConversationPresenter
import net.ketc.numeri.presentation.view.activity.ui.ConversationActivityUI
import net.ketc.numeri.presentation.view.activity.ui.IConversationActivityUI
import net.ketc.numeri.presentation.view.component.TweetViewHolder
import net.ketc.numeri.presentation.view.component.adapter.TwitterRecyclerAdapter
import net.ketc.numeri.util.android.DialogOwner
import net.ketc.numeri.util.android.defaultInit
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity

class ConversationActivity : ApplicationActivity<ConversationPresenter>(), ConversationActivityInterface,
        IConversationActivityUI by ConversationActivityUI() {

    override val ctx: Context = this
    override val presenter = ConversationPresenter(this)
    private val dialogOwner = DialogOwner()
    override val statusId: Long by lazy { intent.getLongExtra(EXTRA_STATUS_ID, -1) }
    override val clientId: Long by lazy { intent.getLongExtra(EXTRA_CLIENT_ID, -1) }
    private var mClient: TwitterClient? = null
    private val adapter by lazy {
        TwitterRecyclerAdapter(presenter, {
            TweetViewHolder(ctx, presenter, client) {
                presenter.onClickTweet(it)
            }
        })
    }
    override var client: TwitterClient
        set(value) {
            mClient = value
            tweetsRecycler.adapter = adapter
        }
        get() = mClient ?: throw IllegalStateException()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        tweetsRecycler.defaultInit()
        presenter.initialize()
    }

    override fun onPause() {
        super.onPause()
        dialogOwner.onPause()
    }

    override fun onResume() {
        super.onResume()
        dialogOwner.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogOwner.onDestroy()
    }

    override fun insert(tweet: Tweet) {
        adapter.insertTop(tweet)
        tweetsRecycler.scrollToPosition(0)
    }

    override fun showDialog(dialog: AppCompatDialog) {
        dialogOwner.showDialog(dialog)
    }

    companion object {
        fun start(ctx: Context, statusId: Long, clientId: Long) {
            ctx.startActivity<ConversationActivity>(EXTRA_STATUS_ID to statusId,
                    EXTRA_CLIENT_ID to clientId)
        }

        private val EXTRA_STATUS_ID = "EXTRA_STATUS_ID"
        private val EXTRA_CLIENT_ID = "EXTRA_CLIENT_ID"
    }
}

interface ConversationActivityInterface : ActivityInterface {
    val statusId: Long
    val clientId: Long
    var client: TwitterClient
    fun insert(tweet: Tweet)
    fun showDialog(dialog: AppCompatDialog)
}