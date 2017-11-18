package tech.ketc.numeri.ui.model.delegate

import tech.ketc.numeri.domain.repository.ITwitterStreamRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.client.TwitterStream
import tech.ketc.numeri.domain.twitter.client.getStream

class StreamHandler(private val mRepository: ITwitterStreamRepository) : IStreamHandler {
    override fun getStream(client: TwitterClient): TwitterStream = client.getStream(mRepository)
}
