package tech.ketc.numeri.domain

import android.arch.lifecycle.LiveData
import tech.ketc.numeri.domain.twitter.client.ITwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import twitter4j.User

interface ITwitterUserRepository {
    val latestUpdatedLiveData: LiveData<TwitterUser>

    fun createOrGet(client: ITwitterClient, user: User): TwitterUser
}