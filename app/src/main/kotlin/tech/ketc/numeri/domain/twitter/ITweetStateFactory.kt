package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.TweetState
import twitter4j.Status

interface ITweetStateFactory {
    fun getOrPutState(client: TwitterClient, status: Status): TweetState
    fun updateState(client: TwitterClient, id: Long, isFav: Boolean, isRt: Boolean): TweetState
    fun get(client: TwitterClient, tweet: Tweet): TweetState
    fun get(client: TwitterClient, status: Status): TweetState?
    fun getRetweetedId(client: TwitterClient, tweet: Tweet): Long?
}