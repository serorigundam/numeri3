package tech.ketc.numeri.ui.model

import android.annotation.SuppressLint
import android.arch.lifecycle.*
import android.content.Intent
import android.util.ArrayMap
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.repository.*
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.client.getUserList
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.domain.twitter.model.UserList
import tech.ketc.numeri.domain.twitter.twitterCallbackUrl
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.ui.model.delegate.*
import tech.ketc.numeri.util.arch.coroutine.ResponseDeferred
import tech.ketc.numeri.util.arch.coroutine.asyncResponse
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

@SuppressLint("StaticFieldLeak")
class MainViewModel @Inject constructor(private val mApp: App,
                                        private val mAccountRepository: IAccountRepository,
                                        private val mTimelineRepository: ITimelineInfoRepository,
                                        private val mUserRepository: ITwitterUserRepository,
                                        imageRepository: IImageRepository,
                                        streamRepository: ITwitterStreamRepository)
    : ViewModel(),
        IImageLoadable by ImageLoadable(imageRepository),
        IClientHandler by ClientHandler(mAccountRepository, mUserRepository),
        IStreamHandler by StreamHandler(streamRepository),
        IUserHandler by UserHandler(mUserRepository),
        ITimelineChangeObserver by TimelineChangeObserver(mTimelineRepository),
        ITimelineInfoReader by TimelineInfoReader(mApp, mTimelineRepository) {

    private val mGroupLock = ReentrantLock()

    fun createAuthorizationURL() = asyncResponse { mAccountRepository.createAuthorizationURL() }

    private fun createNewClient(oauthVerifier: String)
            = asyncResponse { mAccountRepository.createTwitterClient(oauthVerifier) }

    fun onNewIntent(intent: Intent): ResponseDeferred<TwitterClient>? {
        val data = intent.data ?: return null
        val oauthVerifier = data.getQueryParameter("oauth_verifier") ?: return null
        if (!data.toString().startsWith(mApp.twitterCallbackUrl)) return null
        return createNewClient(oauthVerifier)
    }

    fun createNameList(clientUsers: List<Pair<TwitterClient, TwitterUser>>,
                       infoList: List<TimelineInfo>): ResponseDeferred<List<String>> {
        return asyncResponse {
            val map = ArrayMap<TwitterClient, List<UserList>>()
            clientUsers.map { it.first }.forEach {
                map.put(it, it.getUserList(mUserRepository))
            }
            infoList.map { toName(it, clientUsers, map) }
        }
    }

    fun loadGroupListBlocking() = asyncResponse {
        mGroupLock.withLock { mTimelineRepository.getGroupList() }
    }
}