package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.infra.entity.AccountToken

interface ITwitterClientFactory {
    fun create(token: AccountToken): TwitterClient
}