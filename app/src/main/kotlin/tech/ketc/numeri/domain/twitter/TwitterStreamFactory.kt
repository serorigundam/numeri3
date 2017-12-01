package tech.ketc.numeri.domain.twitter

import android.os.Handler
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.repository.ITweetRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.*
import tech.ketc.numeri.domain.twitter.client.TwitterStream
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.arch.StreamSource
import tech.ketc.numeri.util.logTag
import twitter4j.*
import twitter4j.TwitterStreamFactory
import java.lang.Exception
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.read
import kotlin.concurrent.write

class TwitterStreamFactory @Inject constructor(private val app: App) : ITwitterStreamFactory {

    private val mMap = LinkedHashMap<Long, TwitterStream>()
    private val mLock = ReentrantReadWriteLock()

    override fun createOrGet(client: TwitterClient,
                             tweetRepository: ITweetRepository,
                             userRepository: ITwitterUserRepository): TwitterStream
            = mLock.read { mMap[client.id] }
            ?: TwitterStreamInternal(app, client, tweetRepository, userRepository)
            .also { mLock.write { mMap.put(client.id, it) } }


    private class TwitterStreamInternal(app: App,
                                        private val client: TwitterClient,
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

        val mLatestTweet = StreamSource<Tweet>()
        val mLatestTweetDeletionNotice = StreamSource<StatusDeletionNotice>()
        val mLatestFavoriteNotice = StreamSource<StatusNotice>()
        val mLatestUnFavoriteNotice = StreamSource<StatusNotice>()
        val mLatestFollowNotice = StreamSource<UserNotice>()
        val mLatestUnFollowNotice = StreamSource<UserNotice>()
        val mLatestBlockNotice = StreamSource<UserNotice>()
        val mLatestUnBlockNotice = StreamSource<UserNotice>()

        override val latestTweet = mLatestTweet.stream()
        override val latestTweetDeletionNotice = mLatestTweetDeletionNotice.stream()
        override val latestFavoriteNotice = mLatestFavoriteNotice.stream()
        override val latestUnFavoriteNotice = mLatestUnFavoriteNotice.stream()
        override val latestFollowNotice = mLatestFollowNotice.stream()
        override val latestUnFollowNotice = mLatestUnFollowNotice.stream()
        override val latestBlockNotice = mLatestBlockNotice.stream()
        override val latestUnBlockNotice = mLatestUnBlockNotice.stream()

        private val loggerHandler = Handler()

        private fun User.toTwitterUser() = userRepository.createOrGet(this)
        private fun Status.toTweet() = tweetRepository.createOrUpdate(client, this)

        override fun onUserListMemberAddition(addedMember: User, listOwner: User, list: UserList) {
        }

        override fun onFavorite(source: User, target: User, favoritedStatus: Status) {
            val tweet = favoritedStatus.toTweet()
            mLatestFavoriteNotice.post(StatusNotice(source.toTwitterUser(),
                    target.toTwitterUser(),
                    tweet))
            if (source.id == client.id) {
                tweetRepository.updateState(client, favoritedStatus.id, isFav = true)
            }
        }

        override fun onBlock(source: User, blockedUser: User) {
            mLatestBlockNotice.post(UserNotice(source.toTwitterUser(), blockedUser.toTwitterUser()))
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
            loggerHandler.post {
                ex.printStackTrace()
            }
        }

        override fun onDeletionNotice(directMessageId: Long, userId: Long) {
        }

        override fun onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {
            tweetRepository.get(statusDeletionNotice.statusId)
                    ?.takeIf { it.user.id == client.id }
                    ?.takeIf { it.retweetedTweet != null }
                    ?.let { tweet ->
                        Logger.v(logTag, "unretweet $tweet")
                        val retweetedTweet = tweet.retweetedTweet!!
                        tweetRepository.updateState(client, retweetedTweet.id, isRt = false)
                    }
            tweetRepository.deleteById(statusDeletionNotice.statusId)
            mLatestTweetDeletionNotice.post(statusDeletionNotice)
        }

        override fun onUserProfileUpdate(updatedUser: User) {
            userRepository.createOrGet(updatedUser)
        }

        override fun onDirectMessage(directMessage: DirectMessage) {
        }

        override fun onUserListUnsubscription(subscriber: User, listOwner: User, list: UserList) {
        }

        override fun onFollow(source: User, followedUser: User) {
            mLatestFollowNotice.post(UserNotice(source.toTwitterUser(), followedUser.toTwitterUser()))
        }

        override fun onUserListMemberDeletion(deletedMember: User, listOwner: User, list: UserList) {
        }

        override fun onUserListDeletion(listOwner: User, list: UserList) {
        }

        override fun onUnfollow(source: User, unfollowedUser: User) {
            mLatestUnFollowNotice.post(UserNotice(source.toTwitterUser(), unfollowedUser.toTwitterUser()))
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
            val tweet = unfavoritedStatus.toTweet()
            mLatestUnFavoriteNotice.post(StatusNotice(source.toTwitterUser(),
                    target.toTwitterUser(),
                    tweet))
            if (source.id == client.id) {
                tweetRepository.updateState(client, unfavoritedStatus.id, isFav = false)
            }
        }

        override fun onUserDeletion(deletedUser: Long) {
        }

        override fun onFriendList(friendIds: LongArray) {
        }

        override fun onUnblock(source: User, unblockedUser: User) {
            mLatestUnBlockNotice.post(UserNotice(source.toTwitterUser(), unblockedUser.toTwitterUser()))
        }

        override fun onStatus(status: Status) {
            val tweet = status.toTweet()
            mLatestTweet.post(tweet)
            if (status.user.id == client.id && status.isRetweet) {
                tweetRepository.updateState(client, status.retweetedStatus.id, isRt = true)
            }

        }

        override fun onUserSuspension(suspendedUser: Long) {
        }
    }
}