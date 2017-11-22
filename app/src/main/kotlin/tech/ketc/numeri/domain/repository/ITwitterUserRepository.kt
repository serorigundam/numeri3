package tech.ketc.numeri.domain.repository

import android.arch.lifecycle.LiveData
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.domain.twitter.model.UserList
import twitter4j.User

interface ITwitterUserRepository {
    val latestUpdatedUser: LiveData<TwitterUser>
    val latestDeletedUser: LiveData<TwitterUser>

    fun createOrGet(user: User): TwitterUser
    fun show(client: TwitterClient, id: Long): TwitterUser
    fun delete(user: TwitterUser)
    fun getUserList(client: TwitterClient, user: TwitterUser): List<UserList>
    fun reloadUserList(client: TwitterClient, user: TwitterUser): List<UserList>
}