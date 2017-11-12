package tech.ketc.numeri.domain.twitter

import android.os.Build
import android.text.Html
import tech.ketc.numeri.domain.twitter.model.*
import tech.ketc.numeri.util.unmodifiableList
import twitter4j.Status
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class TweetFactory : ITweetFactory {

    private val map = LinkedHashMap<Long, TweetInternal>()
    private val updateListeners = ArrayList<TweetUpdateListener>()
    private val deleteListeners = ArrayList<TweetUpdateListener>()

    private val lock = ReentrantReadWriteLock()

    override fun createOrGet(userFactory: ITwitterUserFactory, status: Status): Tweet {
        val tweet = lock.read { map[status.id] }
        return tweet?.also { it.updateAndCallback(status) }
                ?: lock.write { TweetInternal(this, userFactory, status).also { map.put(it.id, it) } }
    }

    private fun TweetInternal.updateAndCallback(status: Status) {
        if (update(status)) updateListeners.forEach { it(this) }
    }

    override fun addUpdateListener(listener: TweetUpdateListener) {
        updateListeners.add(listener)
    }

    override fun removeUpdateListener(listener: TweetUpdateListener) {
        updateListeners.remove(listener)
    }

    override fun delete(tweet: Tweet) {
        lock.read { map[tweet.id] } ?: return
        deleteListeners.forEach { it(tweet) }
        lock.write { map.remove(tweet.id) }
    }

    override fun deleteByUser(user: TwitterUser) {
        lock.read { map.filter { it.value.user.id == user.id } }.forEach { (key, value) ->
            deleteListeners.forEach { it(value) }
            lock.write { map.remove(key) }
        }
    }

    override fun addDeleteListener(listener: TweetDeleteListener) {
        deleteListeners.add(listener)
    }

    override fun removeListener(listener: TweetDeleteListener) {
        deleteListeners.remove(listener)
    }

    private class TweetInternal(tweetFactory: TweetFactory, userFactory: ITwitterUserFactory, status: Status) : Tweet {

        override val id: Long = status.id
        override val user: TwitterUser = userFactory.createOrGet(status.user)
        override val createdAt: String = status.createdAt.format()
        override val text: String = status.text
        override val quotedTweet: Tweet? = status.quotedStatus?.run { tweetFactory.createOrGet(userFactory, this) }
        override val retweetedTweet: Tweet? = status.retweetedStatus?.run { tweetFactory.createOrGet(userFactory, this) }
        override val source: String = status.source?.let { fromHtml(it).toString() } ?: ""
        override val favoriteCount: Int
            get() = mFavoriteCount
        override val retweetCount: Int
            get() = mRetweetCount
        override val hashtags: List<String> = status.hashtagEntities.map { it.text }.unmodifiableList()
        override val urlEntities: List<UrlEntity> = status.urlEntities.map(::UrlEntity).unmodifiableList()
        override val mediaEntities: List<MediaEntity> = status.mediaEntities.map(::MediaEntity).unmodifiableList()
        override val userMentionEntities: List<UserMentionEntity> = status.userMentionEntities.map(::UserMentionEntity).unmodifiableList()
        override val inReplyToStatusId: Long = status.inReplyToStatusId

        var mFavoriteCount: Int = status.favoriteCount
        var mRetweetCount: Int = status.retweetCount

        fun update(status: Status): Boolean {
            if (!isUpdate(status)) return false
            mFavoriteCount = status.favoriteCount
            mRetweetCount = status.retweetCount
            return true
        }

        fun isUpdate(status: Status): Boolean {
            if (status.id != this.id) throw IllegalArgumentException("status with a different ID was given")
            return mFavoriteCount != status.favoriteCount
                    || mRetweetCount != status.favoriteCount
        }

        override fun equals(other: Any?): Boolean {
            if (other !is Tweet) return false
            return id == other.id
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }

        override fun toString(): String {
            return "$user,$text"
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