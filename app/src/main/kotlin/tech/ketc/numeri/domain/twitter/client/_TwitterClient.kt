package tech.ketc.numeri.domain.twitter.client

import tech.ketc.numeri.domain.repository.ITwitterStreamRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository

fun TwitterClient.getUser(repository: ITwitterUserRepository)
        = repository.createOrGet(twitter.showUser(id))

fun TwitterClient.getStream(repository: ITwitterStreamRepository)
        = repository.createOrGet(this)
