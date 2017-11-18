package tech.ketc.numeri.ui.model

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.repository.*
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.isMention
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.infra.element.TlType
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.ui.model.delegate.*
import tech.ketc.numeri.ui.view.recycler.timeline.TimeLineDataSource
import twitter4j.Paging
import javax.inject.Inject

class TimeLineViewModel @Inject constructor(accountRepository: AccountRepository,
                                            userRepository: ITwitterUserRepository,
                                            imageRepository: IImageRepository,
                                            streamRepository: ITwitterStreamRepository,
                                            private val mTweetRepository: ITweetRepository)
    : ViewModel(),
        IClientHandler by ClientHandler(accountRepository, userRepository),
        IImageLoadable by ImageLoadable(imageRepository) {

    private var mClient: TwitterClient? = null
        get() {
            return field ?: throw IllegalStateException()
        }

    private var mTimelineInfo: TimelineInfo? = null
        get() {
            return field ?: throw IllegalStateException()
        }

    val storeTweetsLiveData = MutableLiveData<List<Tweet>>()

    private val mStream by lazy { StreamHandler(streamRepository).getStream(mClient!!) }

    val dataSource by lazy {
        val info = mTimelineInfo!!
        TimeLineDataSource(createDataSourceDelegate(mTweetRepository, info.foreignId, info.type, mClient!!))
    }

    fun initialize(timelineInfo: TimelineInfo, owner: LifecycleOwner, callback: (TwitterClient) -> Unit, error: (Throwable) -> Unit) {
        mTimelineInfo = timelineInfo
        clients.observe(owner) {
            it.ifPresent {
                mClient = it.find { it.id == timelineInfo.accountId }
                callback(mClient!!)
            }
            it.ifError { error(it) }
        }
    }

    private fun createOnStatusHandler(type: TlType): OnStatusHandler? = when (type) {
        TlType.HOME -> HomeHandler()
        TlType.MENTIONS -> MentionsHandler()
        TlType.USER_LIST -> null
        TlType.PUBLIC -> null
        TlType.FAVORITE -> null
    }

    fun startStream(owner: LifecycleOwner, handle: (Tweet) -> Unit): Boolean {
        val type = mTimelineInfo!!.type
        val handler = createOnStatusHandler(type) ?: return false
        mStream.latestTweet.observe(owner) { tweet ->
            handler.onStatus(mClient!!, tweet, handle)
        }
        return true
    }

    companion object {

        private val home: TwitterClient.(Paging, ITweetRepository) -> MutableList<Tweet> = { paging, repo ->
            twitter.getHomeTimeline(paging).map { repo.createOrUpdate(it) }.toMutableList()
        }
        private val mentions: TwitterClient.(Paging, ITweetRepository) -> MutableList<Tweet> = { paging, repo ->
            twitter.getMentionsTimeline(paging).map { repo.createOrUpdate(it) }.toMutableList()
        }
        private val userList: TwitterClient.(Paging, ITweetRepository, Long) -> MutableList<Tweet> = { paging, repo, listId ->
            twitter.getUserListStatuses(listId, paging).map { repo.createOrUpdate(it) }.toMutableList()
        }
        private val public: TwitterClient.(Paging, ITweetRepository, Long) -> MutableList<Tweet> = { paging, repo, userId ->
            twitter.getUserTimeline(userId, paging).map { repo.createOrUpdate(it) }.toMutableList()
        }
        private val favorite: TwitterClient.(Paging, ITweetRepository, Long) -> MutableList<Tweet> = { paging, repo, userId ->
            twitter.getFavorites(userId, paging).map { repo.createOrUpdate(it) }.toMutableList()
        }

        private fun createDataSourceDelegate(repository: ITweetRepository, foreignId: Long, type: TlType,
                                             client: TwitterClient): (Paging) -> MutableList<Tweet> = when (type) {
            TlType.HOME -> {
                { client.home(it, repository) }
            }
            TlType.MENTIONS -> {
                { client.mentions(it, repository) }
            }
            TlType.USER_LIST -> {
                { client.userList(it, repository, foreignId) }
            }
            TlType.PUBLIC -> {
                { client.public(it, repository, foreignId) }
            }
            TlType.FAVORITE -> {
                { client.favorite(it, repository, foreignId) }
            }
        }
    }

    private interface OnStatusHandler {
        fun onStatus(client: TwitterClient, tweet: Tweet, handle: (Tweet) -> Unit)
    }

    private class HomeHandler : OnStatusHandler {
        override fun onStatus(client: TwitterClient, tweet: Tweet, handle: (Tweet) -> Unit) {
            handle(tweet)
        }
    }

    private class MentionsHandler : OnStatusHandler {
        override fun onStatus(client: TwitterClient, tweet: Tweet, handle: (Tweet) -> Unit) {
            if (tweet.isMention(client)) handle(tweet)
        }
    }

}