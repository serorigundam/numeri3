package tech.ketc.numeri.domain

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import tech.ketc.numeri.domain.twitter.ITwitterUserFactory
import tech.ketc.numeri.domain.twitter.UserUpdateListener
import tech.ketc.numeri.domain.twitter.client.ITwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.util.arch.livedata.map
import twitter4j.User
import javax.inject.Inject

class TwitterUserRepository @Inject constructor(private val userFactory: ITwitterUserFactory) : ITwitterUserRepository, UserUpdateListener {

    init {
        userFactory.addUpdateListener(this)
    }

    private val mLatestUpdatedUser = MutableLiveData<TwitterUser>()

    override val latestUpdatedLiveData: LiveData<TwitterUser>
        get() = mLatestUpdatedUser.map { it }

    override fun createOrGet(client: ITwitterClient, user: User): TwitterUser {
        return userFactory.createOrGet(client, user)
    }


    override fun invoke(user: TwitterUser) {
        mLatestUpdatedUser.value = user
    }
}