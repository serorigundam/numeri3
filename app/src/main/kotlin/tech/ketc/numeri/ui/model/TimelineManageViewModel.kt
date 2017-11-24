package tech.ketc.numeri.ui.model

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.util.ArrayMap
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.repository.IAccountRepository
import tech.ketc.numeri.domain.repository.ITimelineInfoRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.client.getUser
import tech.ketc.numeri.domain.twitter.client.getUserList
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.domain.twitter.model.UserList
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.ui.model.delegate.ClientHandler
import tech.ketc.numeri.ui.model.delegate.IClientHandler
import tech.ketc.numeri.ui.model.delegate.ITimelineInfoReader
import tech.ketc.numeri.ui.model.delegate.TimelineInfoReader
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.arch.coroutine.asyncResponse
import tech.ketc.numeri.util.arch.coroutine.bindLaunch
import tech.ketc.numeri.util.logTag
import tech.ketc.numeri.util.unmodifiableList
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class TimelineManageViewModel
@Inject constructor(private val mApp: App,
                    private val mTimelineRepository: ITimelineInfoRepository,
                    private val mAccountRepository: IAccountRepository,
                    private val mUserRepository: ITwitterUserRepository)
    : ViewModel(),
        ITimelineInfoReader by TimelineInfoReader(mApp, mTimelineRepository),
        IClientHandler by ClientHandler(mAccountRepository, mUserRepository) {

    private val mClientToUserListsMap = ArrayMap<TwitterClient, List<UserList>>()
    private val mClients = ArrayList<Pair<TwitterClient, TwitterUser>>()
    private var initialized = false
    private var mLock = ReentrantLock()

    val clientUsers: List<Pair<TwitterClient, TwitterUser>>
        get() = mClients.unmodifiableList()

    fun userList(client: TwitterClient) = mClientToUserListsMap[client]!!

    fun createGroup(groupName: String) = asyncResponse {
        mTimelineRepository.createGroup(groupName)
    }

    fun deleteGroup(group: TimelineGroup) = asyncResponse {
        mTimelineRepository.deleteGroup(group)
    }

    fun initialize(owner: LifecycleOwner, callback: () -> Unit, error: (Throwable) -> Unit) {
        bindLaunch(owner) {
            val response = asyncResponse {
                mLock.withLock {
                    val clients = mAccountRepository.clients()
                    if (!initialized) {
                        Logger.v(logTag, "initialize")
                        clients.forEach { client ->
                            val lists = client.getUserList(mUserRepository)
                            val user = client.getUser(mUserRepository)
                            mClients.add(client to user)
                            mClientToUserListsMap.put(client, lists)
                            initialized = true
                        }
                    }
                }
            }.await()
            response.ifError(error)
            response.ifPresent { callback() }
        }
    }

    fun toName(info: TimelineInfo) = toName(info, mClients, mClientToUserListsMap)

    fun joinToGroup(groupName: String, info: TimelineInfo) = asyncResponse {
        val i = mTimelineRepository.getInfo(info.type, info.accountId, info.foreignId)
        mTimelineRepository.joinToGroup(TimelineGroup(groupName), i)
        return@asyncResponse i
    }

    fun replace(groupName: String, from: TimelineInfo, to: TimelineInfo) = asyncResponse {
        mTimelineRepository.replace(TimelineGroup(groupName), from, to)
    }

    fun insert(groupName: String, info: TimelineInfo, order: Int) = asyncResponse {
        mTimelineRepository.insert(TimelineGroup(groupName), info, order)
    }

    fun removeFromGroup(groupName: String, info: TimelineInfo) = asyncResponse {
        mTimelineRepository.removeFromGroup(TimelineGroup(groupName), info)
    }

    fun notifyTimelineChanged() {
        mTimelineRepository.notifyDataChanged()
    }

}