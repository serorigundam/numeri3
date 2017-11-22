package tech.ketc.numeri.ui.model.delegate

import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.util.arch.coroutine.ResponseDeferred

interface IClientHandler {
    fun clients(): ResponseDeferred<Set<TwitterClient>>
    fun getClientUser(client: TwitterClient): ResponseDeferred<TwitterUser>
    fun getClientUsers(clients: Set<TwitterClient>): ResponseDeferred<List<Pair<TwitterClient, TwitterUser>>>
}