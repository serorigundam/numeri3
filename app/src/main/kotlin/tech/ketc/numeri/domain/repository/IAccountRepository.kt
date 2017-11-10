package tech.ketc.numeri.domain.repository

import tech.ketc.numeri.domain.twitter.client.ITwitterClient

interface IAccountRepository {

    fun createAuthorizationURL(): String

    fun createTwitterClient(oauthVerifier: String): ITwitterClient

    fun clients(): Set<ITwitterClient>

    fun deleteClient(twitterClient: ITwitterClient)
}