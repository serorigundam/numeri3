package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.repository.ITweetRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.TwitterStream
import tech.ketc.numeri.domain.twitter.client.TwitterClient

interface ITwitterStreamFactory {
    fun createOrGet(client: TwitterClient,
                    tweetRepository: ITweetRepository,
                    userRepository: ITwitterUserRepository): TwitterStream
}