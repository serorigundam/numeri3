package tech.ketc.numeri.domain.twitter.client

import tech.ketc.numeri.domain.repository.ITwitterStreamRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository

fun TwitterClient.getUser(repository: ITwitterUserRepository)
        = repository.show(this, id)

fun TwitterClient.getStream(repository: ITwitterStreamRepository)
        = repository.createOrGet(this)

fun TwitterClient.getUserList(repository: ITwitterUserRepository)
        = repository.getUserList(this, getUser(repository))

fun TwitterClient.reloadUserList(repository: ITwitterUserRepository)
        = repository.reloadUserList(this, getUser(repository))