package tech.ketc.numeri.ui.model.delegate

import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.client.TwitterStream

interface IStreamHandler {
    fun getStream(client: TwitterClient): TwitterStream
}