package tech.ketc.numeri.ui.model.delegate

import tech.ketc.numeri.domain.repository.IAccountRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.client.getUser
import tech.ketc.numeri.util.arch.coroutine.asyncResponse

class ClientHandler(private val mAccountRepository: IAccountRepository,
                    private val mUserRepository: ITwitterUserRepository)
    : IClientHandler {
    override fun clients() = asyncResponse { mAccountRepository.clients() }

    override fun getClientUser(client: TwitterClient) = asyncResponse {
        client.getUser(mUserRepository)
    }

    override fun getClientUsers(clients: Set<TwitterClient>) = asyncResponse {
        clients.map { it to it.getUser(mUserRepository) }
    }
}