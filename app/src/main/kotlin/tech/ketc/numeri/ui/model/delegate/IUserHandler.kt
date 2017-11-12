package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LiveData
import tech.ketc.numeri.domain.twitter.model.TwitterUser

interface IUserHandler {
    val latestUpdatedUser: LiveData<TwitterUser>
    val latestDeletedUser: LiveData<TwitterUser>
}