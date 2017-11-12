package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.repository.ITweetRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import twitter4j.Status
import twitter4j.User

fun User.toTwitterUser(repository: ITwitterUserRepository)
        = repository.createOrGet(this)

fun Status.toTweet(tweetRepository: ITweetRepository)
        = tweetRepository.createOrUpdate(this)