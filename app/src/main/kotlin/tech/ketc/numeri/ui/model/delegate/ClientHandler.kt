package tech.ketc.numeri.ui.model.delegate

import kotlinx.coroutines.experimental.async
import tech.ketc.numeri.domain.repository.IAccountRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.client.getUser
import tech.ketc.numeri.util.arch.response.response

class ClientHandler(private val mAccountRepository: IAccountRepository,
                    private val mUserRepository: ITwitterUserRepository)
    : IClientHandler {
    override fun clients() = async {
        response { mAccountRepository.clients() }
    }

    override fun getClientUser(client: TwitterClient) = async {
        response { client.getUser(mUserRepository) }
    }

    override fun getClientUsers(clients: Set<TwitterClient>) = async {
        response { clients.map { it to it.getUser(mUserRepository) } }
    }
}