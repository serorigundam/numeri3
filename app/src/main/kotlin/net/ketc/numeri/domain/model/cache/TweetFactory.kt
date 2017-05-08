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

    fun create(twitterClient: TwitterClient, status: Status, update: Boolean = false): Tweet {
        val tweet = TweetCache.putOrGet(status)
        fun setTweetState(t: Tweet) {
            if (update) {
                val updateStatus = twitterClient.twitter.showStatus(status.id)
                FavoriteStateCache.changeState(twitterClient, t, updateStatus.isFavorited)
                RetweetStateCache.changeState(twitterClient, t, updateStatus.isRetweeted)
            } else {
                FavoriteStateCache.changeState(twitterClient, t, status.isFavorited)
                RetweetStateCache.changeState(twitterClient, t, status.isRetweeted)
            }
            t.retweetedTweet?.let { retweet ->
                var s: Status? = null
                if (RetweetStateCache.getStateOrNull(twitterClient, retweet) == null) {
                    s = twitterClient.twitter.showStatus(retweet.id)
                    RetweetStateCache.changeState(twitterClient, retweet, s.isRetweeted)
                }
                if (FavoriteStateCache.getStateOrNull(twitterClient, retweet) == null) {
                    val s2 = s ?: twitterClient.twitter.showStatus(retweet.id)
                    FavoriteStateCache.changeState(twitterClient, retweet, s2.isFavorited)
                }
            }
            t.quotedTweet?.let(::setTweetState)
        }
        setTweetState(tweet)
        return tweet
    }
}

fun Status.convert(twitterClient: TwitterClient) = TweetFactory.create(twitterClient, this)

fun Status.updateAndCache(twitterClient: TwitterClient) = TweetFactory.create(twitterClient, this, true)

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
        override val mediaEntities: List<MediaEntity> = status.mediaEntities.map(::MediaEntity).toImmutableList()
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
            @Suppress("DEPRECATION")
            private fun fromHtml(str: String) = if (Build.VERSION.SDK_INT >= 24) {
                Html.fromHtml(str, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(str)
            }
        }
    }
}

fun TwitterClient.getTweet(id: Long) = TweetFactory.get(id) ?: twitter.showStatus(id).convert(this)