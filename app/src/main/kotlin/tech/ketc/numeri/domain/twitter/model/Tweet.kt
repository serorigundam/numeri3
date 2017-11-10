package tech.ketc.numeri.domain.twitter.model

interface Tweet {
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