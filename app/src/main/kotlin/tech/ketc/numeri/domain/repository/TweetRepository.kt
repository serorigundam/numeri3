package tech.ketc.numeri.domain.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import tech.ketc.numeri.domain.twitter.*
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.util.arch.livedata.map
import twitter4j.Status
import javax.inject.Inject

class TweetRepository @Inject constructor(private val mTweetFactory: ITweetFactory,
                                          private val mUserFactory: ITwitterUserFactory)
    : ITweetRepository {

    private val mLatestUpdateTweet = MutableLiveData<Tweet>()
    private val mLatestDeletedTweet = MutableLiveData<Tweet>()

    private val updateListener: TweetUpdateListener = {
        mLatestUpdateTweet.postValue(it)
    }

    private val deleteListener: TweetDeleteListener = {
        mLatestDeletedTweet.postValue(it)
    }

    init {
        mTweetFactory.addUpdateListener(updateListener)
        mTweetFactory.addDeleteListener(deleteListener)
    }


    override val latestUpdatedUser: LiveData<Tweet>
        get() = mLatestUpdateTweet.map { it }
    override val latestDeletedUser: LiveData<Tweet>
        get() = mLatestDeletedTweet.map { it }

    override fun createOrUpdate(client: TwitterClient, status: Status): Tweet {
        return mTweetFactory.createOrGet(client, mUserFactory, status)
    }

    override fun delete(tweet: Tweet) {
        mTweetFactory.delete(tweet)
    }

    override fun deleteByUser(user: TwitterUser) {
        mTweetFactory.deleteByUser(user)
    }
}