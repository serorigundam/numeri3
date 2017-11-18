package tech.ketc.numeri.domain.repository

import tech.ketc.numeri.domain.twitter.ITwitterStreamFactory
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.client.TwitterStream
import javax.inject.Inject

class TwitterStreamRepository @Inject constructor(private val mFactory: ITwitterStreamFactory,
                                                  private val mUserRepository: ITwitterUserRepository,
                                                  private val mTweetRepository: ITweetRepository)
    : ITwitterStreamRepository {

    override fun createOrGet(client: TwitterClient): TwitterStream =
            mFactory.createOrGet(client, mTweetRepository, mUserRepository)
}