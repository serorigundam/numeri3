package tech.ketc.numeri.domain.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import tech.ketc.numeri.domain.twitter.ITweetFactory
import tech.ketc.numeri.domain.twitter.TweetDeleteListener
import tech.ketc.numeri.domain.twitter.TweetUpdateListener
import tech.ketc.numeri.domain.twitter.TwitterUserFactory
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.util.arch.livedata.map
import twitter4j.Status
import javax.inject.Inject

class TweetRepository @Inject constructor(private val tweetFactory: ITweetFactory,
                                          private val userFactory: TwitterUserFactory)
    : ITweetRepository {

    private val mLatestUpdateTweet = MutableLiveData<Tweet>()
    private val mLatestDeletedTweet = MutableLiveData<Tweet>()

    private val updateListener: TweetUpdateListener = {
        mLatestUpdateTweet.value = it
    }

    private val deleteListener: TweetDeleteListener = {
        mLatestDeletedTweet.value = it
    }

    init {
        tweetFactory.addUpdateListener(updateListener)
        tweetFactory.addDeleteListener(deleteListener)
    }


    override val latestUpdatedUser: LiveData<Tweet>
        get() = mLatestUpdateTweet.map { it }
    override val latestDeletedUser: LiveData<Tweet>
        get() = mLatestDeletedTweet.map { it }

    override fun createOrUpdate(client: TwitterClient, status: Status): Tweet {
        return tweetFactory.createOrGet(client, userFactory, status)
    }

    override fun delete(tweet: Tweet) {
        tweetFactory.delete(tweet)
    }
}