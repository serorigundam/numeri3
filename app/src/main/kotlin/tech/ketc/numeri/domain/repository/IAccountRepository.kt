package tech.ketc.numeri.domain.repository

import tech.ketc.numeri.domain.twitter.client.TwitterClient

interface IAccountRepository {

    fun createAuthorizationURL(): String

    fun createTwitterClient(oauthVerifier: String): TwitterClient

    fun clients(): Set<TwitterClient>

    fun deleteClient(twitterClient: TwitterClient)
}