package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LifecycleOwner
import tech.ketc.numeri.domain.repository.IAccountRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.client.getUser
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.util.arch.BindingLifecycleAsyncTask
import tech.ketc.numeri.util.arch.livedata.AsyncLiveData
import tech.ketc.numeri.util.arch.response.Response

class ClientHandler(accountRepository: IAccountRepository,
                    private val userRepository: ITwitterUserRepository)
    : IClientHandler {

    override val clients = AsyncLiveData { accountRepository.clients() }

    override fun getClientUser(owner: LifecycleOwner, client: TwitterClient, handle: (Response<TwitterUser>) -> Unit)
            = BindingLifecycleAsyncTask { client.getUser(userRepository) }.run(owner, handle)
}