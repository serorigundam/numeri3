package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LifecycleOwner
import tech.ketc.numeri.domain.twitter.client.ITwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.util.arch.livedata.AsyncLiveData
import tech.ketc.numeri.util.arch.response.Response

interface IClientHandler {
    val clients: AsyncLiveData<Set<ITwitterClient>>
    fun getClientUser(owner: LifecycleOwner, client: ITwitterClient, handle: (Response<TwitterUser>) -> Unit)
}