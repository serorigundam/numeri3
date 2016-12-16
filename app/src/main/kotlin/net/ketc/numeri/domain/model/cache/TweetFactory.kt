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

    fun create(twitterClient: TwitterClient, status: Status): Tweet {
        val tweet = TweetCache.putOrGet(status)
        fun setTweetState(t: Tweet) {
            FavoriteStateCache.setState(twitterClient, t, status.isFavorited)
            RetweetStateCache.setState(twitterClient, t, status.isRetweeted)
            t.retweetedTweet?.let(::setTweetState)
            t.quotedTweet?.let(::setTweetState)
        }
        setTweetState(tweet)
        return tweet
    }
}


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

    private class TweetImpl(status: Status) : Tweet {
        override val id: Long = status.id
        override val user: TwitterUser
        override val createdAt: String
        override val text: String
        override val quotedTweet: Tweet?
        override val retweetedTweet: Tweet?
        override val source: String
        override val favoriteCount: Int
            get() = mFavoriteCount
        override val retweetCount: Int
            get() = mRetweetCount
        override val hashtags: List<String>
        override val urlEntities: List<UrlEntity>
        override val mediaEntities: List<MediaEntity>
        override val userMentionEntities: List<UserMentionEntity>

        var mFavoriteCount: Int
        var mRetweetCount: Int

        init {
            user = TwitterUserCache.putOrGet(status.user)
            createdAt = status.createdAt.format()
            text = status.text

            quotedTweet = status.quotedStatus?.run { TweetCache.putOrGet(this) }
            retweetedTweet = status.retweetedStatus?.run { TweetCache.putOrGet(this) }

            source = fromHtml(status.source ?: "").toString()

            mFavoriteCount = status.favoriteCount
            mRetweetCount = status.retweetCount

            hashtags = status.hashtagEntities.map { it.text }.toImmutableList()
            urlEntities = status.urlEntities.map(::UrlEntity).toImmutableList()
            mediaEntities = status.extendedMediaEntities.map(::MediaEntity).toImmutableList()
            userMentionEntities = status.userMentionEntities.map(::UserMentionEntity).toImmutableList()
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

