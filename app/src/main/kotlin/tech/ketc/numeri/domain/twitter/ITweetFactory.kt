package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import twitter4j.Status


interface ITweetFactory {
    fun createOrGet(client: TwitterClient, userFactory: ITwitterUserFactory, status: Status): Tweet

    fun addUpdateListener(listener: TweetUpdateListener)

    fun removeUpdateListener(listener: TweetUpdateListener)

    fun delete(tweet: Tweet)

    fun deleteById(id:Long)

    fun deleteByUser(user: TwitterUser)

    fun addDeleteListener(listener: TweetDeleteListener)

    fun removeListener(listener: TweetDeleteListener)

    fun get(id: Long):Tweet?
}