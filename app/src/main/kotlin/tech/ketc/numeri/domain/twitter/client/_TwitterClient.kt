package tech.ketc.numeri.domain.twitter.client

import tech.ketc.numeri.domain.repository.ITweetRepository
import tech.ketc.numeri.domain.repository.ITwitterStreamRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.model.Tweet

fun TwitterClient.getUser(repository: ITwitterUserRepository)
        = repository.show(this, id)

fun TwitterClient.getStream(repository: ITwitterStreamRepository)
        = repository.createOrGet(this)

fun TwitterClient.getUserList(repository: ITwitterUserRepository)
        = repository.getUserList(this, getUser(repository))

fun TwitterClient.reloadUserList(repository: ITwitterUserRepository)
        = repository.reloadUserList(this, getUser(repository))

fun TwitterClient.showTweet(repository: ITweetRepository, id: Long): Tweet {
    return repository.get(id) ?: repository.createOrUpdate(this, twitter.showStatus(id))
}