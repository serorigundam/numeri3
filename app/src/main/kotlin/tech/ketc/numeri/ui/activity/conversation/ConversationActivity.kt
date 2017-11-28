package tech.ketc.numeri.ui.activity.conversation

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.ui.fragment.operation.OperationTweetDialogFragment
import tech.ketc.numeri.ui.model.ConversationViewModel
import tech.ketc.numeri.ui.model.delegate.HasTweetOperator
import tech.ketc.numeri.ui.model.delegate.ITweetOperator
import tech.ketc.numeri.ui.view.recycler.SimpleRecyclerAdapter
import tech.ketc.numeri.ui.view.recycler.timeline.TweetViewHolder
import tech.ketc.numeri.util.android.setFinishWithNavigationClick
import tech.ketc.numeri.util.android.setUpSupportActionbar
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import java.io.Serializable
import javax.inject.Inject

class ConversationActivity : AppCompatActivity(), AutoInject, IConversationUI by ConversationUI(), HasTweetOperator {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: ConversationViewModel by viewModel { mViewModelFactory }
    private val mInfo by lazy { intent.getSerializableExtra(EXTRA_INFO) as Info }

    private var mAdapter: SimpleRecyclerAdapter<TweetViewHolder, Tweet>
            = SimpleRecyclerAdapter(::createViewHolder, ::bindViewHolder)

    override val operator: ITweetOperator
        get() = mModel
    private var mPosition = -1

    companion object {
        private val EXTRA_INFO = "EXTRA_INFO"
        private val TAG_OPERATE = "TAG_OPERATE"
        private val SAVED_POSITION = "SAVED_POSITION"
        fun start(ctx: Context, client: TwitterClient, tweet: Tweet) {
            val info = Info(client, tweet.retweetedTweet ?: tweet)
            ctx.startActivity<ConversationActivity>(EXTRA_INFO to info)
            (ctx as? Activity)?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        setUpSupportActionbar(toolbar)
        toolbar.setFinishWithNavigationClick(this)
        savedInstanceState?.let(this::restore)
        recycler.adapter = mAdapter
        val values = mAdapter.values
        mModel.reserveTweets.takeIf { it.isNotEmpty() }?.let {
            values.addAll(it)
            mAdapter.notifyDataSetChanged()
        }
        if (mPosition != -1)
            values.takeIf { it.isNotEmpty() }?.let {
                recycler.scrollToPosition(mPosition)
            }
        mModel.stream.observe(this) {
            values.add(0, it)
            mAdapter.notifyItemInserted(0)
            mModel.reserveTweets.add(0, it)
            recycler.scrollToPosition(0)
        }
        if (savedInstanceState == null) {
            mAdapter.values.add(mInfo.tweet)
            mModel.traceStart(this, mInfo.client, mInfo.tweet)
        }
    }

    private fun createViewHolder(): TweetViewHolder
            = TweetViewHolder(this, mInfo.client, this, mModel, this::onClickTweetItem)

    private fun bindViewHolder(holder: TweetViewHolder, position: Int) {
        holder.bind(mAdapter.values[position])
    }

    private fun onClickTweetItem(tweet: Tweet) {
        OperationTweetDialogFragment.create(mInfo.client, tweet).show(supportFragmentManager, TAG_OPERATE)
    }

    private fun restore(savedInstanceState: Bundle) {
        mPosition = savedInstanceState.getInt(SAVED_POSITION)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (recycler.childCount > 0)
            mPosition = recycler.getChildAdapterPosition(recycler.getChildAt(0))
        outState.putInt(SAVED_POSITION, mPosition)
        super.onSaveInstanceState(outState)
    }

    data class Info(val client: TwitterClient, val tweet: Tweet) : Serializable
}