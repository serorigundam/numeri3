package net.ketc.numeri.presentation.presenter.activity

import android.os.Bundle
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.cache.getTweet
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.view.activity.ConversationActivityInterface
import net.ketc.numeri.util.copy
import net.ketc.numeri.util.rx.*
import org.jetbrains.anko.toast
import twitter4j.TwitterException

object ConversationPresenterFactory : PresenterFactory<ConversationPresenter>() {
    override fun create() = ConversationPresenter()
}

class ConversationPresenter : AutoDisposablePresenter<ConversationActivityInterface>() {
    private val tweets = ArrayList<Tweet>()

    private var visibleTopPosition = -1

    override fun initialize(savedInstanceState: Bundle?, isStartedForFirst: Boolean) {
        super.initialize(savedInstanceState, isStartedForFirst)
        if (isStartedForFirst) initialize(activity.client, activity.statusId)
        else restore()
    }

    private fun initialize(client: TwitterClient, statusId: Long) {
        singleTask(MySchedulers.twitter) {
            client.getTweet(statusId)
        } safeError {
            it.printStackTrace()
        } safeSuccess {
            tweets.add(it)
            insert(it)
            traceConversation(it.inReplyToStatusId)
        }
    }

    private fun restore() {
        tweets.takeIf { it.isNotEmpty() }?.let {
            activity.insertAll(it.copy())
        }
        visibleTopPosition.takeIf { it != -1 }?.let {
            activity.visibleTopPosition = it
        }
    }

    override fun onPause() {
        visibleTopPosition = activity.visibleTopPosition
        super.onPause()
    }

    private fun createConversationObservable(client: TwitterClient, inReplyToStatusId: Long) = Flowable.create<Tweet>({ emitter ->
        var id = inReplyToStatusId
        while (id != -1L) {
            emitter.safeNext {
                client.getTweet(id)
            }?.let {
                id = it.inReplyToStatusId
            } ?: break
        }
        if (id == -1L)
            emitter.onComplete()
    }, BackpressureStrategy.BUFFER).twitterThread()


    fun traceConversation(inReplyToStatusId: Long) {
        createConversationObservable(activity.client, inReplyToStatusId).subscribeNamed(
                onError = {
                    it.printStackTrace()
                    val code = (it as? TwitterException)?.errorCode?.let { "Code = $it" } ?: ""
                    ctx.toast("error$code")
                },
                onNext = { result ->
                    safePost {
                        tweets.add(0, result)
                        it.insert(result)
                    }
                }).autoDispose()
    }
}