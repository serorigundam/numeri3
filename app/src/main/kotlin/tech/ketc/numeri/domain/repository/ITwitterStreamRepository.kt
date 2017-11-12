package tech.ketc.numeri.domain.repository

import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.client.TwitterStream

interface ITwitterStreamRepository {
    fun createOrGet(client: TwitterClient): TwitterStream
}