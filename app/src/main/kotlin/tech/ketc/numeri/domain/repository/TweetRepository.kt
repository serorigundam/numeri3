package tech.ketc.numeri.domain.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import tech.ketc.numeri.domain.twitter.*
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.TweetState
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.util.arch.livedata.map
import twitter4j.Status
import javax.inject.Inject

class TweetRepository @Inject constructor(private val mTweetFactory: ITweetFactory,
                                          private val mStateFactory: ITweetStateFactory,
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


    override val latestUpdatedTweet: LiveData<Tweet>
        get() = mLatestUpdateTweet.map { it }
    override val latestDeletedTweet: LiveData<Tweet>
        get() = mLatestDeletedTweet.map { it }

    override fun createOrUpdate(client: TwitterClient, status: Status, forceStateGet: Boolean): Tweet {
        return mTweetFactory.createOrGet(client, mUserFactory, status, forceStateGet)
    }

    override fun delete(tweet: Tweet) {
        mTweetFactory.delete(tweet)
    }

    override fun deleteById(id: Long) {
        mTweetFactory.deleteById(id)
    }

    override fun deleteByUser(user: TwitterUser) {
        mTweetFactory.deleteByUser(user)
    }

    override fun getState(client: TwitterClient, tweet: Tweet): TweetState {
        return mStateFactory.get(client, tweet)
    }

    override fun updateState(client: TwitterClient, id: Long, isFav: Boolean?, isRt: Boolean?): TweetState {
        return mStateFactory.updateState(client, id, isFav, isRt)
    }

    override fun getRetweetedId(client: TwitterClient, tweet: Tweet): Long? {
        return mStateFactory.getRetweetedId(client, tweet)
    }

    override fun get(id: Long) = mTweetFactory.get(id)
}