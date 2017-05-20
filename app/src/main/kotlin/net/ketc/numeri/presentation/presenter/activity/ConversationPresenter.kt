package net.ketc.numeri.presentation.presenter.activity

import android.os.Bundle
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.cache.getTweet
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.view.activity.ConversationActivityInterface
import net.ketc.numeri.util.copy
import net.ketc.numeri.util.rx.*
import org.jetbrains.anko.toast
import twitter4j.TwitterException
import javax.inject.Inject

object ConversationPresenterFactory : PresenterFactory<ConversationPresenter>() {
    override fun create() = ConversationPresenter()
}

class ConversationPresenter : AutoDisposablePresenter<ConversationActivityInterface>() {
    @Inject
    lateinit var clientService: OAuthService

    private val tweets = ArrayList<Tweet>()

    private var visibleTopPosition = -1
    private val client: TwitterClient
        get() = activity.client

    init {
        inject()
    }

    override fun initialize(savedInstanceState: Bundle?, isStartedForFirst: Boolean) {
        super.initialize(savedInstanceState, isStartedForFirst)
        if (isStartedForFirst) initialize()
        else restore()
    }

    private fun initialize() {
        singleTask(MySchedulers.twitter) {
            client.getTweet(activity.statusId)
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

    private fun createConversationObservable(inReplyToStatusId: Long) = Flowable.create<Tweet>({ emitter ->
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
        createConversationObservable(inReplyToStatusId).subscribeNamed(
                onError = {
                    ctx.toast("error${"Code" + (it as? TwitterException)?.errorCode}")
                },
                onNext = { result ->
                    safePost {
                        tweets.add(0, result)
                        it.insert(result)
                    }
                }).autoDispose()
    }
}