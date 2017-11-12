package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LiveData
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.model.TwitterUser

class UserHandler(repository: ITwitterUserRepository) : IUserHandler {
    override val latestUpdatedUser: LiveData<TwitterUser> = repository.latestUpdatedUser
    override val latestDeletedUser: LiveData<TwitterUser> = repository.latestDeletedUser
}