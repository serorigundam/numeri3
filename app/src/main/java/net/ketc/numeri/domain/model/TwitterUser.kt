package net.ketc.numeri.domain.model

import net.ketc.numeri.domain.model.cache.Cacheable
import twitter4j.User


interface TwitterUser : Cacheable<Long> {
    val name: String
    val screenName: String
    val location: String
    val description: String
    val iconUrl: String
    val originalIconUrl: String
    val headerImageUrl: String
    val profileBackgroundColor: String
    val isProtected: Boolean
    val followersCount: Int
    val friendsCount: Int
    val urlEntities: List<UrlEntity>
}

internal class TwitterUserImpl(user: User) : TwitterUser {
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
    override val headerImageUrl: String
        get() = mHeaderImageUrl
    override val profileBackgroundColor: String
        get() = mProfileBackgroundColor
    override val isProtected: Boolean
        get() = mIsProtected
    override val followersCount: Int
        get() = mFollowersCount
    override val friendsCount: Int
        get() = mFriendsCount
    override val urlEntities: List<UrlEntity>
        get() = mUrlEntities

    private var mName: String
    private var mScreenName: String
    private var mLocation: String
    private var mDescription: String
    private var mIconUrl: String
    private var mOriginalIconUrl: String
    private var mHeaderImageUrl: String
    private var mProfileBackgroundColor: String
    private var mIsProtected: Boolean
    private var mFollowersCount: Int
    private var mFriendsCount: Int
    private var mUrlEntities: List<UrlEntity>

    var updateCallback: (TwitterUser) -> Unit = {}


    init {
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
        mUrlEntities = user.descriptionURLEntities.orEmpty().map(::UrlEntity)
    }

    fun update(user: User) {
        if (!isUpdate(user))
            return
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

        val descriptionURLEntities = user.descriptionURLEntities.orEmpty()
        if (urlEntities.size != descriptionURLEntities.size) {
            mUrlEntities = descriptionURLEntities.map(::UrlEntity)
        } else {
            urlEntities.forEachIndexed { i, urlEntity ->
                if (descriptionURLEntities[i].displayURL != urlEntity.displayUrl) {
                    mUrlEntities = descriptionURLEntities.map(::UrlEntity)
                    return@forEachIndexed
                }
            }
        }
        updateCallback(this)
    }

    fun isUpdate(user: User): Boolean {
        if (equals(user)) {
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
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is TwitterUser -> id == other.id
            is User -> id == other.id
            else -> false
        }
    }

    override fun hashCode() = id.hashCode()

}