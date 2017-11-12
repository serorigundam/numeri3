package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.repository.ITweetRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import twitter4j.Status
import twitter4j.User

fun User.toTwitterUser(client: TwitterClient, repository: ITwitterUserRepository)
        = repository.createOrGet(client, this)

fun Status.toTweet(client: TwitterClient, tweetRepository: ITweetRepository)
        = tweetRepository.createOrUpdate(client, this)