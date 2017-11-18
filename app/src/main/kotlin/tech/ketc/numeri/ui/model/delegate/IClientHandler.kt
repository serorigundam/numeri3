package tech.ketc.numeri.ui.model.delegate

import kotlinx.coroutines.experimental.Deferred
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.util.arch.response.Response

interface IClientHandler {
    fun clients(): Deferred<Response<Set<TwitterClient>>>
    fun getClientUser(client: TwitterClient): Deferred<Response<TwitterUser>>
    fun getClientUsers(clients: Set<TwitterClient>): Deferred<Response<List<Pair<TwitterClient, TwitterUser>>>>
}