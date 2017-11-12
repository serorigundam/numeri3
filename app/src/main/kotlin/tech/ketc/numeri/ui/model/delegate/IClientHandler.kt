package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LifecycleOwner
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.util.arch.livedata.AsyncLiveData
import tech.ketc.numeri.util.arch.response.Response

interface IClientHandler {
    val clients: AsyncLiveData<Set<TwitterClient>>
    fun getClientUser(owner: LifecycleOwner, client: TwitterClient, handle: (Response<TwitterUser>) -> Unit)
}