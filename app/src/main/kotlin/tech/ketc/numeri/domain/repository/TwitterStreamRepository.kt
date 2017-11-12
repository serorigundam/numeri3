package tech.ketc.numeri.domain.repository

import tech.ketc.numeri.domain.twitter.ITwitterStreamFactory
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.client.TwitterStream
import javax.inject.Inject

class TwitterStreamRepository @Inject constructor(private val factory: ITwitterStreamFactory,
                                                  private val userRepository: ITwitterUserRepository,
                                                  private val tweetRepository: ITweetRepository)
    : ITwitterStreamRepository {

    override fun createOrGet(client: TwitterClient): TwitterStream =
            factory.createOrGet(client, tweetRepository, userRepository)
}