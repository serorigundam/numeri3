package net.ketc.numeri.domain.model.cache

import android.os.Build
import android.text.Html
import net.ketc.numeri.domain.model.*
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.util.toImmutableList
import twitter4j.Status
import java.text.SimpleDateFormat
import java.util.*

object TweetFactory {
    fun get(id: Long) = TweetCache.get(id)

    fun get(predicate: (Tweet) -> Boolean) = TweetCache.find(predicate)

    fun delete(id: Long) {
        TweetCache.get(id)?.let {
            FavoriteStateCache.deleteState(it)
            RetweetStateCache.deleteState(it)
            TweetCache.delete(id)
        }

    }

    fun create(twitterClient: TwitterClient, status: Status): Tweet {
        val tweet = TweetCache.putOrGet(status)
        fun setTweetState(t: Tweet) {
            FavoriteStateCache.changeState(twitterClient, t, status.isFavorited)
            RetweetStateCache.changeState(twitterClient, t, status.isRetweeted)
            t.retweetedTweet?.let {
                RetweetStateCache.changeState(twitterClient, it, twitterClient.isMyTweet(tweet))
                FavoriteStateCache.changeState(twitterClient, it, status.isFavorited)
            }
            t.quotedTweet?.let(::setTweetState)
        }
        setTweetState(tweet)
        return tweet
    }
}

fun Status.convertAndCacheOrGet(twitterClient: TwitterClient) = TweetFactory.create(twitterClient, this)

private object TweetCache : ConversionCache<Status, Tweet, Long> {
    private val map = LinkedHashMap<Long, Tweet>()

    override fun get(id: Long) = map[id]

    override fun putOrGet(obj: Status): Tweet {
        val id = obj.id
        val tweet = map[id]
        return if (tweet == null) {
            val tweetImpl = TweetImpl(obj)
            map.put(id, tweetImpl)
            tweetImpl
        } else {
            tweet
        }
    }

    fun find(predicate: (Tweet) -> Boolean) = map.map { it.value }.find(predicate)

    fun delete(id: Long) {
        map.remove(id)
    }

    private class TweetImpl(status: Status) : Tweet {
        override val id: Long = status.id
        override val user: TwitterUser = TwitterUserCache.putOrGet(status.user)
        override val createdAt: String = status.createdAt.format()
        override val text: String = status.text
        override val quotedTweet: Tweet? = status.quotedStatus?.run { TweetCache.putOrGet(this) }
        override val retweetedTweet: Tweet? = status.retweetedStatus?.run { TweetCache.putOrGet(this) }
        override val source: String = fromHtml(status.source ?: "").toString()
        override val favoriteCount: Int
            get() = mFavoriteCount
        override val retweetCount: Int
            get() = mRetweetCount
        override val hashtags: List<String> = status.hashtagEntities.map { it.text }.toImmutableList()
        override val urlEntities: List<UrlEntity> = status.urlEntities.map(::UrlEntity).toImmutableList()
        override val mediaEntities: List<MediaEntity> = status.extendedMediaEntities.map(::MediaEntity).toImmutableList()
        override val userMentionEntities: List<UserMentionEntity> = status.userMentionEntities.map(::UserMentionEntity).toImmutableList()
        override val inReplyToStatusId: Long = status.inReplyToStatusId

        var mFavoriteCount: Int = status.favoriteCount
        var mRetweetCount: Int = status.retweetCount

        override fun toString(): String {
            return StringBuilder()
                    .append("id : ").append(id)
                    .append("\nuser : ").append(user)
                    .append("\ntext : ").append(text)
                    .append("\ncreatedAt : ").append(createdAt)
                    .toString()
        }

        companion object {
            private val DATE_FORMAT = "yyyy/MM/dd HH:mm:ss"
            private fun Date.format(): String = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(this)
            private fun fromHtml(str: String) = if (Build.VERSION.SDK_INT >= 24) {
                Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(str)
            }
        }
    }
}

fun TwitterClient.getTweet(id: Long) = TweetFactory.get(id) ?: twitter.showStatus(id).convertAndCacheOrGet(this)