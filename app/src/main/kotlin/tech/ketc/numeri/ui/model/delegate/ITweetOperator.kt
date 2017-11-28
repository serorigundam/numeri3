package tech.ketc.numeri.ui.model.delegate

import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.TweetState
import tech.ketc.numeri.util.arch.coroutine.ResponseDeferred

interface ITweetOperator {
    fun favorite(client: TwitterClient, tweet: Tweet): ResponseDeferred<TweetState>

    fun unfavorite(client: TwitterClient, tweet: Tweet): ResponseDeferred<TweetState>

    fun retweet(client: TwitterClient, tweet: Tweet): ResponseDeferred<TweetState>

    fun unretweet(client: TwitterClient, tweet: Tweet): ResponseDeferred<TweetState>

    fun delete(client: TwitterClient, tweet: Tweet): ResponseDeferred<Unit>

    fun getState(client: TwitterClient, tweet: Tweet): TweetState
}

interface HasTweetOperator {
    val operator: ITweetOperator
}