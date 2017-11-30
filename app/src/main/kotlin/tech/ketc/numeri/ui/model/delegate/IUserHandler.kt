package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LiveData
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.util.arch.coroutine.ResponseDeferred

interface IUserHandler {
    val latestUpdatedUser: LiveData<TwitterUser>
    val latestDeletedUser: LiveData<TwitterUser>
    fun show(client: TwitterClient, id: Long): ResponseDeferred<TwitterUser>
}