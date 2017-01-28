package net.ketc.numeri.presentation.presenter.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.cache.*
import net.ketc.numeri.domain.model.isMyTweet
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.view.component.TweetMenuItemsInterface
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.MySchedulers

class TweetOperatorDialogPresenter(private val ctx: Context, autoDisposable: AutoDisposable,
                                   private val tweetMenuDialog: TweetMenuItemsInterface,
                                   private val error: (Throwable) -> Unit) : AutoDisposable by autoDisposable {
    private val tweet: Tweet = tweetMenuDialog.tweet
    private val client: TwitterClient = tweetMenuDialog.client

    fun changeFavorite() {
        tweetMenuDialog.isFavoriteMenuClickable = false
        singleTask(MySchedulers.twitter) {
            if (!tweetMenuDialog.isFavorite) {
                client.twitter.createFavorite(tweet.id)
            } else {
                client.twitter.destroyFavorite(tweet.id)
            }.convertAndCacheOrGet(client)
        } error {
            error(it)
            tweetMenuDialog.isFavoriteMenuClickable = true
        } success {
            tweetMenuDialog.isFavorite = client.isFavorite(it)
            tweetMenuDialog.isFavoriteMenuClickable = true
        }
    }

    fun changeRetweeted() {
        tweetMenuDialog.isRetweetMenuClickable = false
        singleTask(MySchedulers.twitter) {
            if (!tweetMenuDialog.isRetweeted) {
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
            tweetMenuDialog.isRetweetMenuClickable = true
        } success {
            RetweetStateCache.changeState(client, tweet, it)
            tweetMenuDialog.isRetweeted = it
            tweetMenuDialog.isRetweetMenuClickable = true
        }
    }

    fun openUri(urlStr: String) {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlStr)))
    }

}