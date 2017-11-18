package tech.ketc.numeri.domain.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import tech.ketc.numeri.domain.twitter.ITwitterUserFactory
import tech.ketc.numeri.domain.twitter.UserDeleteListener
import tech.ketc.numeri.domain.twitter.UserUpdateListener
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.util.arch.livedata.mediate
import twitter4j.User
import javax.inject.Inject

class TwitterUserRepository @Inject constructor(private val mUserFactory: ITwitterUserFactory,
                                                private val mTweetRepository: ITweetRepository) : ITwitterUserRepository {

    private val updateListener: UserUpdateListener = {
        mLatestUpdatedUser.postValue(it)
    }

    private val deleteListener: UserDeleteListener = {
        mLatestDeletedUser.postValue(it)
    }

    init {
        mUserFactory.addUpdateListener(updateListener)
        mUserFactory.addDeleteListener(deleteListener)
    }

    private val mLatestUpdatedUser = MutableLiveData<TwitterUser>()
    private val mLatestDeletedUser = MutableLiveData<TwitterUser>()

    override val latestUpdatedUser: LiveData<TwitterUser>
        get() = mLatestUpdatedUser.mediate()

    override val latestDeletedUser: LiveData<TwitterUser>
        get() = mLatestDeletedUser.mediate()

    override fun createOrGet(user: User): TwitterUser {
        return mUserFactory.createOrGet(user)
    }

    override fun show(client: TwitterClient, id: Long): TwitterUser {
        return mUserFactory.get(id) ?: createOrGet(client.twitter.showUser(id))
    }

    override fun delete(user: TwitterUser) {
        mTweetRepository.deleteByUser(user)
        mUserFactory.delete(user)
    }
}