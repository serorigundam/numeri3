package tech.ketc.numeri.domain.repository

import android.arch.lifecycle.LiveData
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import twitter4j.Status

interface ITweetRepository {
    val latestUpdatedUser: LiveData<Tweet>
    val latestDeletedUser: LiveData<Tweet>
    fun createOrUpdate(client: TwitterClient, status: Status): Tweet
    fun delete(tweet: Tweet)
    fun deleteByUser(user: TwitterUser)
}