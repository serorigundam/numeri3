package tech.ketc.numeri.ui.model

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.repository.*
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.infra.element.TlType
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.ui.model.delegate.ClientHandler
import tech.ketc.numeri.ui.model.delegate.IClientHandler
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.ui.model.delegate.ImageLoadable
import tech.ketc.numeri.ui.view.recycler.timeline.TimeLineDataSource
import twitter4j.Paging
import javax.inject.Inject

class TimeLineViewModel @Inject constructor(accountRepository: AccountRepository,
                                            userRepository: ITwitterUserRepository,
                                            imageRepository: IImageRepository,
                                            private val tweetRepository: ITweetRepository)
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

    val dataSource by lazy {
        TimeLineDataSource {
            val foreignId = mTimelineInfo!!.foreignId
            when (mTimelineInfo!!.type) {
                TlType.HOME -> mClient!!.home(it, tweetRepository)
                TlType.MENTIONS -> mClient!!.mentions(it, tweetRepository)
                TlType.USER_LIST -> mClient!!.userList(it, tweetRepository, foreignId)
                TlType.PUBLIC -> mClient!!.public(it, tweetRepository, foreignId)
                TlType.FAVORITE -> mClient!!.favorite(it, tweetRepository, foreignId)
            }
        }
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
    }
}