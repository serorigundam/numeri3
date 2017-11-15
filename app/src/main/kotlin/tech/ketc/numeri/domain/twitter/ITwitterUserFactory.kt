package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.twitter.model.TwitterUser
import twitter4j.User

interface ITwitterUserFactory {

    fun createOrGet(user: User): TwitterUser

    fun get(id: Long): TwitterUser?

    fun addUpdateListener(listener: UserUpdateListener)

    fun removeUpdateListener(listener: UserUpdateListener)

    fun delete(user: TwitterUser)


    fun addDeleteListener(listener: UserDeleteListener)

    fun removeListener(listener: UserDeleteListener)
}