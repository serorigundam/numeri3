package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LiveData
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.util.arch.coroutine.asyncResponse

class UserHandler(private val mRepository: ITwitterUserRepository) : IUserHandler {
    override val latestUpdatedUser: LiveData<TwitterUser> = mRepository.latestUpdatedUser
    override val latestDeletedUser: LiveData<TwitterUser> = mRepository.latestDeletedUser
    override fun show(client: TwitterClient, id: Long) = asyncResponse {
        mRepository.show(client, id)
    }
}