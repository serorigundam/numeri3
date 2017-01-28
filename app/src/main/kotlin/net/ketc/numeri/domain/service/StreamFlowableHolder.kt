package net.ketc.numeri.domain.service

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposables
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.cache.*
import net.ketc.numeri.util.rx.streamThread
import net.ketc.numeri.util.twitter.TwitterApp
import twitter4j.*
import java.util.*
import net.ketc.numeri.domain.service.AvoidingAccessErrors.addStreamListener

interface StreamFlowableHolder {
    val onStatusFlowable: Flowable<Tweet>
    val onDeletionNoticeFlowable: Flowable<StatusDeletionNotice>
    val onFavoriteFlowable: Flowable<StatusNotice>
    val onUnFavoriteFlowable: Flowable<StatusNotice>
    val onFollowFlowable: Flowable<UserNotice>
    val onUnFollowFlowable: Flowable<UserNotice>
    val onBlockFlowable: Flowable<UserNotice>
    val onUnBlockFlowable: Flowable<UserNotice>
}

class StreamFlowableHolderImpl(twitterApp: TwitterApp, twitterClient: TwitterClient) : StreamFlowableHolder {

    private val streamObserver: StreamObserver

    init {
        val stream: TwitterStream = TwitterStreamFactory().instance
        stream.setOAuthConsumer(twitterApp.apiKey, twitterApp.apiSecret)
        stream.oAuthAccessToken = twitterClient.twitter.oAuthAccessToken
        streamObserver = StreamObserver(stream, twitterClient)

    }

    override val onStatusFlowable = Flowable.create<Tweet>({ emitter ->
        val adapter = object : AbstractStreamAdapter() {
            override fun onStatus(tweet: Tweet) {
                emitter.onNext(tweet)
            }
        }
        streamObserver.add(adapter)
        emitter.setDisposable(Disposables.fromAction { streamObserver.remove(adapter) })
    }, BackpressureStrategy.BUFFER)!!.onBackpressureBuffer().streamThread()

    override val onDeletionNoticeFlowable = Flowable.create<StatusDeletionNotice>({ emitter ->
        val adapter = object : AbstractStreamAdapter() {
            override fun onDeletionNotice(notice: StatusDeletionNotice) {
                emitter.onNext(notice)
            }
        }
        streamObserver.add(adapter)
        emitter.setDisposable(Disposables.fromAction { streamObserver.remove(adapter) })
    }, BackpressureStrategy.BUFFER)!!.onBackpressureBuffer().streamThread()

    override val onFavoriteFlowable = Flowable.create<StatusNotice>({ emitter ->
        val adapter = object : AbstractStreamAdapter() {
            override fun onFavorite(notice: StatusNotice) {
                emitter.onNext(notice)
            }
        }
        streamObserver.add(adapter)
        emitter.setDisposable(Disposables.fromAction { streamObserver.remove(adapter) })
    }, BackpressureStrategy.BUFFER)!!.onBackpressureBuffer().streamThread()

    override val onUnFavoriteFlowable = Flowable.create<StatusNotice>({ emitter ->
        val adapter = object : AbstractStreamAdapter() {
            override fun onUnfavorite(notice: StatusNotice) {
                emitter.onNext(notice)
            }
        }
        streamObserver.add(adapter)
        emitter.setDisposable(Disposables.fromAction { streamObserver.remove(adapter) })
    }, BackpressureStrategy.BUFFER)!!.onBackpressureBuffer().streamThread()

    override val onFollowFlowable = Flowable.create<UserNotice>({ emitter ->
        val adapter = object : AbstractStreamAdapter() {
            override fun onFollow(notice: UserNotice) {
                emitter.onNext(notice)
            }
        }
        streamObserver.add(adapter)
        emitter.setDisposable(Disposables.fromAction { streamObserver.remove(adapter) })
    }, BackpressureStrategy.BUFFER)!!.onBackpressureBuffer().streamThread()

    override val onUnFollowFlowable = Flowable.create<UserNotice>({ emitter ->
        val adapter = object : AbstractStreamAdapter() {
            override fun onUnfollow(notice: UserNotice) {
                emitter.onNext(notice)
            }
        }
        streamObserver.add(adapter)
        emitter.setDisposable(Disposables.fromAction { streamObserver.remove(adapter) })
    }, BackpressureStrategy.BUFFER)!!.onBackpressureBuffer().streamThread()

    override val onBlockFlowable = Flowable.create<UserNotice>({ emitter ->
        val adapter = object : AbstractStreamAdapter() {
            override fun onBlock(notice: UserNotice) {
                emitter.onNext(notice)
            }
        }
        streamObserver.add(adapter)
        emitter.setDisposable(Disposables.fromAction { streamObserver.remove(adapter) })
    }, BackpressureStrategy.BUFFER)!!.onBackpressureBuffer().streamThread()

    override val onUnBlockFlowable = Flowable.create<UserNotice>({ emitter ->
        val adapter = object : AbstractStreamAdapter() {
            override fun onUnblock(notice: UserNotice) {
                emitter.onNext(notice)
            }
        }
        streamObserver.add(adapter)
        emitter.setDisposable(Disposables.fromAction { streamObserver.remove(adapter) })
    }, BackpressureStrategy.BUFFER)!!.onBackpressureBuffer().streamThread()

    private class StreamObserver(stream: TwitterStream, val client: TwitterClient) : UserStreamListener {

        init {
            addStreamListener(stream, this)
            stream.user()
        }

        private val streamAdapterList = ArrayList<StreamAdapter>()

        fun add(streamAdapter: StreamAdapter) {
            streamAdapterList.add(streamAdapter)
        }

        fun remove(streamAdapter: StreamAdapter) {
            streamAdapterList.remove(streamAdapter)
        }

        override fun onUserListMemberAddition(addedMember: User, listOwner: User, list: UserList) {
            val source = addedMember.convertAndCacheOrGet()
            val target = listOwner.convertAndCacheOrGet()
            streamAdapterList.forEach {
                it.onUserListMemberAddition(UserListMemberNotice(source, target, list))
            }
        }

        override fun onFavorite(source: User, target: User, favoritedStatus: Status) {
            val source1 = source.convertAndCacheOrGet()
            val target1 = target.convertAndCacheOrGet()
            val isRetweeted = TweetFactory.get(favoritedStatus.id)?.let {
                client.isRetweeted(it)
            }
            val tweet = favoritedStatus.convertAndCacheOrGet(client)
            if (source.id == client.id) {
                client.setFavorite(tweet)
            }
            isRetweeted?.let {
                RetweetStateCache.changeState(client, tweet, it)
            }
            streamAdapterList.forEach {
                it.onFavorite(StatusNotice(source1, target1, tweet))
            }
        }

        override fun onBlock(source: User, blockedUser: User) {
            val source1 = source.convertAndCacheOrGet()
            val target = blockedUser.convertAndCacheOrGet()
            streamAdapterList.forEach {
                it.onBlock(UserNotice(source1, target))
            }
        }

        override fun onUserListUpdate(listOwner: User, list: UserList) {
            val source = listOwner.convertAndCacheOrGet()
            streamAdapterList.forEach {
                it.onUserListUpdate(UserListNotice(source, list))
            }
        }

        override fun onScrubGeo(userId: Long, upToStatusId: Long) {
            streamAdapterList.forEach {
                it.onScrubGeo(IdNotice(userId, upToStatusId))
            }
        }

        override fun onUserListSubscription(subscriber: User, listOwner: User, list: UserList) {
            val source = subscriber.convertAndCacheOrGet()
            val target = listOwner.convertAndCacheOrGet()
            streamAdapterList.forEach {
                it.onUserListSubscription(UserListMemberNotice(source, target, list))
            }
        }

        override fun onQuotedTweet(source: User, target: User, quotingTweet: Status) {
        }

        override fun onDeletionNotice(directMessageId: Long, userId: Long) {
            streamAdapterList.forEach {
                it.onDeletionNotice(IdNotice(directMessageId, userId))
            }
        }

        override fun onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {
            streamAdapterList.forEach {
                it.onDeletionNotice(statusDeletionNotice)
            }
            TweetFactory.delete(statusDeletionNotice.statusId)
        }

        override fun onException(ex: Exception) {
            streamAdapterList.forEach {
                it.onException(ex)
            }
        }

        override fun onUserProfileUpdate(updatedUser: User) {
            val user = updatedUser.convertAndCacheOrGet()
            streamAdapterList.forEach {
                it.onUserProfileUpdate(user)
            }
        }

        override fun onDirectMessage(directMessage: DirectMessage) {
            streamAdapterList.forEach {
                it.onDirectMessage(directMessage)
            }
        }

        override fun onUserListUnsubscription(subscriber: User, listOwner: User, list: UserList) {
            val source = subscriber.convertAndCacheOrGet()
            val target = listOwner.convertAndCacheOrGet()
            streamAdapterList.forEach {
                it.onUserListUnsubscription(UserListMemberNotice(source, target, list))
            }
        }

        override fun onFollow(source: User, followedUser: User) {
            val source1 = source.convertAndCacheOrGet()
            val target = followedUser.convertAndCacheOrGet()
            streamAdapterList.forEach {
                it.onFollow(UserNotice(source1, target))
            }
        }

        override fun onUserListMemberDeletion(deletedMember: User, listOwner: User, list: UserList) {
            val source = deletedMember.convertAndCacheOrGet()
            val target = listOwner.convertAndCacheOrGet()
            streamAdapterList.forEach {
                it.onUserListMemberDeletion(UserListMemberNotice(source, target, list))
            }
        }

        override fun onUserListDeletion(listOwner: User, list: UserList) {
            val source = listOwner.convertAndCacheOrGet()
            streamAdapterList.forEach {
                it.onUserListDeletion(UserListNotice(source, list))
            }
        }

        override fun onUnfollow(source: User, unfollowedUser: User) {
            val source1 = source.convertAndCacheOrGet()
            val target = unfollowedUser.convertAndCacheOrGet()
            streamAdapterList.forEach {
                it.onUnfollow(UserNotice(source1, target))
            }
        }

        override fun onRetweetedRetweet(source: User, target: User, retweetedStatus: Status) {
            val source1 = source.convertAndCacheOrGet()
            val target1 = target.convertAndCacheOrGet()
            val tweet = retweetedStatus.convertAndCacheOrGet(client)
            streamAdapterList.forEach {
                it.onRetweetedRetweet(StatusNotice(source1, target1, tweet))
            }
        }

        override fun onUserListCreation(listOwner: User, list: UserList) {
            val source = listOwner.convertAndCacheOrGet()
            streamAdapterList.forEach {
                it.onUserListCreation(UserListNotice(source, list))
            }
        }

        override fun onFavoritedRetweet(source: User, target: User, favoritedRetweeet: Status) {
        }

        override fun onTrackLimitationNotice(numberOfLimitedStatuses: Int) {
            streamAdapterList.forEach {
                it.onTrackLimitationNotice(numberOfLimitedStatuses)
            }
        }

        override fun onStallWarning(warning: StallWarning) {
            streamAdapterList.forEach {
                it.onStallWarning(warning)
            }
        }

        override fun onUnfavorite(source: User, target: User, unfavoritedStatus: Status) {
            val source1 = source.convertAndCacheOrGet()
            val target1 = target.convertAndCacheOrGet()
            val retweeted = TweetFactory.get(unfavoritedStatus.id)?.let {
                client.isRetweeted(it)
            }
            val tweet = unfavoritedStatus.convertAndCacheOrGet(client)
            if (source.id == client.id) {
                client.setUnFavorite(tweet)
            }
            retweeted?.let {
                RetweetStateCache.changeState(client, tweet, it)
            }
            streamAdapterList.forEach {
                it.onUnfavorite(StatusNotice(source1, target1, tweet))
            }
        }

        override fun onUserDeletion(deletedUser: Long) {
            streamAdapterList.forEach {
                it.onUserDeletion(deletedUser)
            }
        }

        override fun onFriendList(friendIds: LongArray) {
            streamAdapterList.forEach {
                it.onFriendList(friendIds)
            }
        }

        override fun onUnblock(source: User, unblockedUser: User) {
            val source1 = source.convertAndCacheOrGet()
            val target = unblockedUser.convertAndCacheOrGet()
            streamAdapterList.forEach {
                it.onUnblock(UserNotice(source1, target))
            }
        }

        override fun onStatus(status: Status) {
            val tweet = status.convertAndCacheOrGet(client)
            streamAdapterList.forEach {
                it.onStatus(tweet)
            }
        }

        override fun onUserSuspension(suspendedUser: Long) {
            streamAdapterList.forEach {
                it.onUserSuspension(suspendedUser)
            }
        }
    }
}