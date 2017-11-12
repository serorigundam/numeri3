package tech.ketc.numeri.domain.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import tech.ketc.numeri.domain.twitter.ITwitterUserFactory
import tech.ketc.numeri.domain.twitter.UserDeleteListener
import tech.ketc.numeri.domain.twitter.UserUpdateListener
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.util.arch.livedata.map
import twitter4j.User
import javax.inject.Inject

class TwitterUserRepository @Inject constructor(private val userFactory: ITwitterUserFactory,
                                                private val tweetRepository: ITweetRepository) : ITwitterUserRepository {

    private val updateListener: UserUpdateListener = {
        mLatestUpdatedUser.value = it
    }

    private val deleteListener: UserDeleteListener = {
        mLatestDeletedUser.value = it
    }

    init {
        userFactory.addUpdateListener(updateListener)
        userFactory.addDeleteListener(deleteListener)
    }

    private val mLatestUpdatedUser = MutableLiveData<TwitterUser>()
    private val mLatestDeletedUser = MutableLiveData<TwitterUser>()

    override val latestUpdatedUser: LiveData<TwitterUser>
        get() = mLatestUpdatedUser.map { it }

    override val latestDeletedUser: LiveData<TwitterUser>
        get() = mLatestDeletedUser.map { it }

    override fun createOrGet(client: TwitterClient, user: User): TwitterUser {
        return userFactory.createOrGet(client, user)
    }

    override fun delete(user: TwitterUser) {
        tweetRepository.deleteByUser(user)
        userFactory.delete(user)
    }
}