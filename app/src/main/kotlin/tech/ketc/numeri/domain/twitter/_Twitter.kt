package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.repository.ITweetRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import twitter4j.Status
import twitter4j.User

fun User.toTwitterUser(repository: ITwitterUserRepository)
        = repository.createOrGet(this)

fun Status.toTweet(client: TwitterClient, tweetRepository: ITweetRepository)
        = tweetRepository.createOrUpdate(client, this)

infix fun Tweet.isMention(client: TwitterClient) = userMentionEntities.any { it.id == client.id }
        && retweetedTweet == null