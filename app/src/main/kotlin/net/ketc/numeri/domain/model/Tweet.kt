package net.ketc.numeri.domain.model

import net.ketc.numeri.domain.model.cache.Cacheable
import net.ketc.numeri.domain.service.TwitterClient

interface Tweet : Cacheable<Long> {
    val user: TwitterUser
    val createdAt: String
    val text: String
    val quotedTweet: Tweet?
    val retweetedTweet: Tweet?
    val source: String
    val favoriteCount: Int
    val retweetCount: Int
    val hashtags: List<String>
    val urlEntities: List<UrlEntity>
    val mediaEntities: List<MediaEntity>
    val userMentionEntities: List<UserMentionEntity>
    val inReplyToStatusId: Long
}

infix fun TwitterClient.isMyTweet(tweet: Tweet) = tweet.user.id == id
infix fun Tweet.isMention(client: TwitterClient) = userMentionEntities.any { it.id == client.id } && retweetedTweet == null


