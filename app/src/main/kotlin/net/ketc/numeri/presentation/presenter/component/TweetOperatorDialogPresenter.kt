package net.ketc.numeri.presentation.presenter.component

import android.content.Context
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.cache.*
import net.ketc.numeri.domain.model.isMyTweet
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.MySchedulers

class TweetOperatorDialogPresenter(private val ctx: Context, autoDisposable: AutoDisposable,
                                   private val tweet: Tweet,
                                   private val error: (Throwable) -> Unit) : AutoDisposable by autoDisposable {

    fun changeFavorite(client: TwitterClient, error: (Throwable) -> Unit, success: (Boolean) -> Unit) {
        singleTask(MySchedulers.twitter) {
            if (!client.isFavorite(tweet)) {
                client.twitter.createFavorite(tweet.id)
            } else {
                client.twitter.destroyFavorite(tweet.id)
            }.convertAndCacheOrGet(client)
        } error {
            error(it)
            this.error(it)
        } success {
            success(client.isFavorite(it))
        }
    }

    fun changeRetweeted(client: TwitterClient, error: (Throwable) -> Unit, success: (Boolean) -> Unit) {
        singleTask(MySchedulers.twitter) {
            if (!client.isRetweeted(tweet)) {
                client.twitter.retweetStatus(tweet.id)
                true
            } else {
                val myRetweet = TweetFactory.get {
                    client.isMyTweet(it) && it.retweetedTweet != null && it.retweetedTweet!!.id == tweet.id
                }
                myRetweet?.let {
                    client.twitter.destroyStatus(myRetweet.id)
                    TweetFactory.delete(myRetweet.id)
                }
                myRetweet == null
            }
        } error {
            error(it)
            this.error(it)
        } success {
            RetweetStateCache.changeState(client, tweet, it)
            success(it)
        }
    }
}