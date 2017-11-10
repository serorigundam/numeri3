package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.twitter.client.ITwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.domain.twitter.model.IUrlEntity
import tech.ketc.numeri.domain.twitter.model.UrlEntity
import tech.ketc.numeri.util.unmodifiableList
import twitter4j.User
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class TwitterUserFactory : ITwitterUserFactory {

    private val map = LinkedHashMap<Long, TwitterUserInternal>()
    private val updateListeners = ArrayList<UserUpdateListener>()
    private val deleteListeners = ArrayList<UserDeleteListener>()

    private val lock = ReentrantReadWriteLock()

    override fun createOrGet(client: ITwitterClient, user: User): TwitterUser {
        val twitterUser = lock.read { map[user.id] }
        return twitterUser?.also { it.updateAndCallback(user) }
                ?: lock.write { TwitterUserInternal(user).also { map.put(it.id, it) } }
    }

    private fun TwitterUserInternal.updateAndCallback(user: User) {
        if (update(user)) updateListeners.forEach { it(this) }
    }

    override fun addUpdateListener(listener: UserUpdateListener) {
        updateListeners.add(listener)
    }

    override fun removeUpdateListener(listener: UserUpdateListener) {
        updateListeners.remove(listener)
    }

    override fun delete(user: User) {
        map.remove(user.id)
    }

    override fun addDeleteListener(listener: UserDeleteListener) {
        deleteListeners.add(listener)
    }

    override fun removeListener(listener: UserDeleteListener) {
        deleteListeners.remove(listener)
    }

    private class TwitterUserInternal(user: User) : TwitterUser {
        override val id: Long = user.id
        override val name: String
            get() = mName
        override val screenName: String
            get() = mScreenName
        override val location: String
            get() = mLocation
        override val description: String
            get() = mDescription
        override val iconUrl: String
            get() = mIconUrl
        override val originalIconUrl: String
            get() = mOriginalIconUrl
        override val headerImageUrl: String?
            get() = mHeaderImageUrl
        override val profileBackgroundColor: String
            get() = mProfileBackgroundColor
        override val isProtected: Boolean
            get() = mIsProtected
        override val followersCount: Int
            get() = mFollowersCount
        override val friendsCount: Int
            get() = mFriendsCount
        override val favoriteCount: Int
            get() = mFavoriteCount
        override val statusesCount: Int
            get() = mStatusesCount
        override val urlEntities: List<IUrlEntity>
            get() = mUrlEntities.unmodifiableList()

        private var mName: String = user.name ?: ""
        private var mScreenName: String = user.screenName ?: ""
        private var mLocation: String = user.location ?: ""
        private var mDescription: String = user.description ?: ""
        private var mIconUrl: String = user.biggerProfileImageURL
        private var mOriginalIconUrl: String = user.originalProfileImageURL
        private var mHeaderImageUrl: String? = user.profileBannerRetinaURL ?: ""
        private var mProfileBackgroundColor: String = user.profileBackgroundColor
        private var mIsProtected: Boolean = user.isProtected
        private var mFollowersCount: Int = user.followersCount
        private var mFriendsCount: Int = user.friendsCount
        private var mUrlEntities: MutableList<IUrlEntity> = user.descriptionURLEntities.orEmpty().map(::UrlEntity).toMutableList()
        private var mStatusesCount: Int = user.statusesCount
        private var mFavoriteCount: Int = user.favouritesCount

        fun update(user: User): Boolean {
            if (!isUpdate(user)) return false
            mName = user.name ?: ""
            mScreenName = user.screenName ?: ""
            mLocation = user.location ?: ""
            mDescription = user.description ?: ""
            mIconUrl = user.biggerProfileImageURL
            mOriginalIconUrl = user.originalProfileImageURL
            mHeaderImageUrl = user.profileBannerRetinaURL ?: ""
            mProfileBackgroundColor = user.profileBackgroundColor
            mIsProtected = user.isProtected
            mFollowersCount = user.followersCount
            mFriendsCount = user.friendsCount
            mStatusesCount = user.statusesCount
            mFavoriteCount = user.favouritesCount

            val descriptionURLEntities = user.descriptionURLEntities.orEmpty()
            if (urlEntities.size != descriptionURLEntities.size) {
                mUrlEntities = descriptionURLEntities.map(::UrlEntity).toMutableList()
            } else {
                urlEntities.forEachIndexed { i, urlEntity ->
                    if (descriptionURLEntities[i].displayURL != urlEntity.displayUrl) {
                        mUrlEntities[i] = UrlEntity(descriptionURLEntities[i])
                    }
                }
            }
            return true
        }

        fun isUpdate(user: User): Boolean {
            if (user.id != this.id) {
                throw IllegalArgumentException("user with a different ID was given")
            }
            return name != user.name
                    || screenName != user.screenName
                    || location != user.location
                    || description != user.description
                    || iconUrl != user.biggerProfileImageURL
                    || originalIconUrl != originalIconUrl
                    || headerImageUrl != user.profileBannerRetinaURL
                    || profileBackgroundColor != user.profileBackgroundColor
                    || isProtected != user.isProtected
                    || followersCount != user.followersCount
                    || friendsCount != user.friendsCount
                    || statusesCount != user.statusesCount
                    || favoriteCount != user.favouritesCount
        }

        override fun equals(other: Any?): Boolean {
            return if (other is TwitterUser)
                this.id == other.id
            else false
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }
    }
}