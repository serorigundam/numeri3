package net.ketc.numeri.presentation.presenter.activity

import android.os.Bundle
import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.cache.getTweet
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.view.activity.ConversationActivityInterface
import net.ketc.numeri.presentation.view.component.TweetOperatorDialogFactory
import net.ketc.numeri.util.rx.MySchedulers
import org.jetbrains.anko.toast
import twitter4j.TwitterException
import javax.inject.Inject

class ConversationPresenter(override val activity: ConversationActivityInterface) : AutoDisposablePresenter<ConversationActivityInterface>() {

    @Inject
    lateinit var clientService: OAuthService
    lateinit var client: TwitterClient
    lateinit var tweet: Tweet

    init {
        inject()
    }

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        singleTask(MySchedulers.twitter) {
            val client = clientService.clients().single { it.id == activity.clientId }
            client to client.getTweet(activity.statusId)
        } error Throwable::printStackTrace success {
            client = it.first
            activity.client = it.first
            tweet = it.second
            activity.insert(tweet)
            traceConversation(tweet.inReplyToStatusId)
        }
    }

    private fun traceConversation(inReplyToStatusId: Long) {
        if (inReplyToStatusId == -1L) return
        singleTask(MySchedulers.twitter) {
            client.getTweet(inReplyToStatusId)
        } error {
            (it as? TwitterException)?.errorCode?.let {
                ctx.toast("エラーが発生しました。StatusCode=$it")
            }
        } success {
            activity.insert(it)
            traceConversation(it.inReplyToStatusId)
        }
    }

    fun onClickTweet(tweet: Tweet) {
        val dialog = TweetOperatorDialogFactory(ctx, tweet.retweetedTweet ?: tweet, this) {
            it.printStackTrace()
            ctx.toast("error")
        }.create(client)
        activity.showDialog(dialog)
    }
}