package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.twitter.client.ITwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import twitter4j.User

interface ITwitterUserFactory {

    fun createOrGet(client: ITwitterClient, user: User): TwitterUser

    fun addUpdateListener(listener: UserUpdateListener)

    fun removeUpdateListener(listener: UserUpdateListener)

    fun delete(user: User)

    fun addDeleteListener(listener: UserDeleteListener)

    fun removeListener(listener: UserDeleteListener)
}