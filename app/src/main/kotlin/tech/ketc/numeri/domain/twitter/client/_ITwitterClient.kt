package tech.ketc.numeri.domain.twitter.client

import tech.ketc.numeri.domain.repository.ITwitterUserRepository

fun ITwitterClient.getUser(repository: ITwitterUserRepository)
        = repository.createOrGet(this, twitter.showUser(id))
