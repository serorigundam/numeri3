package tech.ketc.numeri.domain.repository

import android.arch.lifecycle.LiveData
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.TweetState
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import twitter4j.Status

interface ITweetRepository {
    val latestUpdatedTweet: LiveData<Tweet>
    val latestDeletedTweet: LiveData<Tweet>
    fun createOrUpdate(client: TwitterClient, status: Status): Tweet
    fun delete(tweet: Tweet)
    fun deleteById(id: Long)
    fun deleteByUser(user: TwitterUser)
    fun getState(client: TwitterClient, tweet: Tweet): TweetState
    fun updateState(client: TwitterClient, id: Long, isFav: Boolean? = null, isRt: Boolean? = null): TweetState
    fun getRetweetedId(client: TwitterClient, tweet: Tweet): Long?
    fun get(id: Long): Tweet?
}