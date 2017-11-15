package tech.ketc.numeri.domain.twitter

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.repository.ITweetRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.*
import tech.ketc.numeri.domain.twitter.client.TwitterStream
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.util.arch.livedata.mediate
import twitter4j.*
import twitter4j.TwitterStreamFactory
import java.lang.Exception
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.read
import kotlin.concurrent.write

class TwitterStreamFactory @Inject constructor(private val app: App) : ITwitterStreamFactory {

    private val map = LinkedHashMap<Long, TwitterStream>()
    private val lock = ReentrantReadWriteLock()

    override fun createOrGet(client: TwitterClient,
                             tweetRepository: ITweetRepository,
                             userRepository: ITwitterUserRepository): TwitterStream
            = lock.read { map[client.id] }
            ?: TwitterStreamInternal(app, client, tweetRepository, userRepository)
            .also { lock.write { map.put(client.id, it) } }


    private class TwitterStreamInternal(app: App,
                                        client: TwitterClient,
                                        private val tweetRepository: ITweetRepository,
                                        private val userRepository: ITwitterUserRepository)
        : TwitterStream, UserStreamListener {
        private val stream = TwitterStreamFactory().instance

        init {
            stream.setOAuthConsumer(app.twitterApiKey, app.twitterSecretKey)
            stream.oAuthAccessToken = client.twitter.oAuthAccessToken
            AvoidingAccessErrors.addStreamListener(stream, this)
            stream.user()
        }

        val mLatestTweet = MutableLiveData<Tweet>()
        val mLatestTweetDeletionNotice = MutableLiveData<StatusDeletionNotice>()
        val mLatestFavoriteNotice = MutableLiveData<StatusNotice>()
        val mLatestUnFavoriteNotice = MutableLiveData<StatusNotice>()
        val mLatestFollowNotice = MutableLiveData<UserNotice>()
        val mLatestUnFollowNotice = MutableLiveData<UserNotice>()
        val mLatestBlockNotice = MutableLiveData<UserNotice>()
        val mLatestUnBlockNotice = MutableLiveData<UserNotice>()

        override val latestTweet: LiveData<Tweet>
            get() = mLatestTweet.mediate()
        override val latestTweetDeletionNotice: LiveData<StatusDeletionNotice>
            get() = mLatestTweetDeletionNotice.mediate()
        override val latestFavoriteNotice: LiveData<StatusNotice>
            get() = mLatestFavoriteNotice.mediate()
        override val latestUnFavoriteNotice: LiveData<StatusNotice>
            get() = mLatestUnFavoriteNotice.mediate()
        override val latestFollowNotice: LiveData<UserNotice>
            get() = mLatestFollowNotice.mediate()
        override val latestUnFollowNotice: LiveData<UserNotice>
            get() = mLatestUnFollowNotice.mediate()
        override val latestBlockNotice: LiveData<UserNotice>
            get() = mLatestBlockNotice.mediate()
        override val latestUnBlockNotice: LiveData<UserNotice>
            get() = mLatestUnBlockNotice.mediate()

        private fun User.toTwitterUser() = toTwitterUser(userRepository)

        private fun Status.toTweet() = toTweet(tweetRepository)

        override fun onUserListMemberAddition(addedMember: User, listOwner: User, list: UserList) {
        }

        override fun onFavorite(source: User, target: User, favoritedStatus: Status) {
            mLatestFavoriteNotice.postValue(StatusNotice(source.toTwitterUser(),
                    target.toTwitterUser(),
                    favoritedStatus.toTweet()))
        }

        override fun onBlock(source: User, blockedUser: User) {
            mLatestBlockNotice.postValue(UserNotice(source.toTwitterUser(), blockedUser.toTwitterUser()))
        }

        override fun onUserListUpdate(listOwner: User, list: UserList) {
        }

        override fun onScrubGeo(userId: Long, upToStatusId: Long) {
        }

        override fun onUserListSubscription(subscriber: User, listOwner: User, list: UserList) {
        }

        override fun onQuotedTweet(source: User, target: User, quotingTweet: Status) {
        }

        override fun onException(ex: Exception) {
        }

        override fun onDeletionNotice(directMessageId: Long, userId: Long) {
        }

        override fun onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {
            mLatestTweetDeletionNotice.postValue(statusDeletionNotice)
        }

        override fun onUserProfileUpdate(updatedUser: User) {
            userRepository.createOrGet(updatedUser)
        }

        override fun onDirectMessage(directMessage: DirectMessage) {
        }

        override fun onUserListUnsubscription(subscriber: User, listOwner: User, list: UserList) {
        }

        override fun onFollow(source: User, followedUser: User) {
            mLatestFollowNotice.postValue(UserNotice(source.toTwitterUser(), followedUser.toTwitterUser()))
        }

        override fun onUserListMemberDeletion(deletedMember: User, listOwner: User, list: UserList) {
        }

        override fun onUserListDeletion(listOwner: User, list: UserList) {
        }

        override fun onUnfollow(source: User, unfollowedUser: User) {
            mLatestUnFollowNotice.postValue(UserNotice(source.toTwitterUser(), unfollowedUser.toTwitterUser()))
        }

        override fun onRetweetedRetweet(source: User, target: User, retweetedStatus: Status) {
        }

        override fun onUserListCreation(listOwner: User, list: UserList) {
        }

        override fun onFavoritedRetweet(source: User, target: User, favoritedRetweeet: Status) {
        }

        override fun onTrackLimitationNotice(numberOfLimitedStatuses: Int) {
        }

        override fun onStallWarning(warning: StallWarning) {
        }

        override fun onUnfavorite(source: User, target: User, unfavoritedStatus: Status) {
            mLatestUnFavoriteNotice.postValue(StatusNotice(source.toTwitterUser(),
                    target.toTwitterUser(),
                    unfavoritedStatus.toTweet()))
        }

        override fun onUserDeletion(deletedUser: Long) {
        }

        override fun onFriendList(friendIds: LongArray) {
        }

        override fun onUnblock(source: User, unblockedUser: User) {
            mLatestUnBlockNotice.postValue(UserNotice(source.toTwitterUser(), unblockedUser.toTwitterUser()))
        }

        override fun onStatus(status: Status) {
            mLatestTweet.postValue(status.toTweet())
        }

        override fun onUserSuspension(suspendedUser: Long) {
        }
    }
}