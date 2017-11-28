package tech.ketc.numeri.domain.twitter

import android.os.Build
import android.text.Html
import android.util.ArrayMap
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.*
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import tech.ketc.numeri.util.unmodifiableList
import twitter4j.Status
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class TweetFactory @Inject constructor(private val mStateFactory: ITweetStateFactory) : ITweetFactory {

    private val mMap = LinkedHashMap<Long, TweetInternal>()
    private val mUpdateListeners = ArrayList<TweetUpdateListener>()
    private val mDeleteListeners = ArrayList<TweetUpdateListener>()

    private val tweetLock = ArrayMap<Long, ReentrantLock>()

    private fun tweetLock(id: Long) = tweetLock.getOrPut(id) { ReentrantLock() }
    private fun unlock(id: Long) = tweetLock.remove(id)

    override fun createOrGet(client: TwitterClient, userFactory: ITwitterUserFactory, status: Status): Tweet {
        Logger.v(logTag, "createOrGet ${status.text}")
        val tweet = mMap[status.id]
        mStateFactory.getOrPutState(client, status)
        return tweet?.also { it.updateAndCallback(status) }
                ?: tweetLock(status.id).withLock {
            TweetInternal(client, this, userFactory, status).also { mMap.put(it.id, it);unlock(status.id) }
        }
    }

    private fun innerStatusCreateOrGet(client: TwitterClient, userFactory: ITwitterUserFactory, status: Status): Tweet {
        val statusId = status.id
        val tweet = mMap[statusId]
        Logger.v(logTag, "innerStatusCreateOrGet id$statusId cached ${tweet != null}")
        return tweet?.also { it.updateAndCallback(status) } ?: tweetLock(statusId).withLock {
            Logger.v(logTag, "innerStatusCreateOrGet locking $statusId")
            TweetInternal(client, this, userFactory, status).also {
                if (mStateFactory.get(client, status) == null) client.twitter.showStatus(statusId).also {
                    Logger.v(logTag, "innerStatusCreateOrGet show $statusId")
                    mStateFactory.getOrPutState(client, it)
                }
                mMap.put(it.id, it)
                unlock(statusId)
                Logger.v(logTag, "innerStatusCreateOrGet unlock $statusId")
            }
        }
    }

    private fun TweetInternal.updateAndCallback(status: Status) {
        if (update(status)) mUpdateListeners.forEach { it(this) }
    }

    override fun addUpdateListener(listener: TweetUpdateListener) {
        mUpdateListeners.add(listener)
    }

    override fun removeUpdateListener(listener: TweetUpdateListener) {
        mUpdateListeners.remove(listener)
    }

    override fun delete(tweet: Tweet) {
        mMap[tweet.id] ?: return
        mDeleteListeners.forEach { it(tweet) }
        tweetLock(tweet.id).withLock { mMap.remove(tweet.id);unlock(tweet.id) }
    }

    override fun deleteById(id: Long) {
        val tweet = mMap[id] ?: return
        mDeleteListeners.forEach { it(tweet) }
        tweetLock(tweet.id).withLock { mMap.remove(tweet.id);unlock(tweet.id) }
    }

    override fun deleteByUser(user: TwitterUser) {
        mMap.filter { it.value.user.id == user.id }.forEach { (key, value) ->
            mDeleteListeners.forEach { it(value) }
            tweetLock(key).withLock { mMap.remove(key);unlock(key) }
        }
    }

    override fun addDeleteListener(listener: TweetDeleteListener) {
        mDeleteListeners.add(listener)
    }

    override fun removeListener(listener: TweetDeleteListener) {
        mDeleteListeners.remove(listener)
    }

    override fun get(id: Long): Tweet? = mMap[id]

    private class TweetInternal(client: TwitterClient, tweetFactory: TweetFactory, userFactory: ITwitterUserFactory, status: Status) : Tweet {

        override val id: Long = status.id
        override val user: TwitterUser = userFactory.createOrGet(status.user)
        override val createdAt: String = status.createdAt.format()
        override val text: String = status.text
        override val quotedTweet: Tweet? = status.quotedStatus?.run { tweetFactory.innerStatusCreateOrGet(client, userFactory, this) }
        override val retweetedTweet: Tweet? = status.retweetedStatus?.run { tweetFactory.innerStatusCreateOrGet(client, userFactory, this) }
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