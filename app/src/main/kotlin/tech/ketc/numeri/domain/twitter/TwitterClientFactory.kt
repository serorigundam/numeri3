package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.App
import tech.ketc.numeri.domain.twitter.client.ITwitterClient
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.infra.entity.AccountToken
import javax.inject.Inject

class TwitterClientFactory @Inject constructor(private val app: App) : ITwitterClientFactory {
    override fun create(token: AccountToken): ITwitterClient = TwitterClient(app, token)
}