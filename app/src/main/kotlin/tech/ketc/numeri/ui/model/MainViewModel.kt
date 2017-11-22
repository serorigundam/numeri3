package tech.ketc.numeri.ui.model

import android.annotation.SuppressLint
import android.arch.lifecycle.*
import android.content.Intent
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.repository.*
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.domain.twitter.twitterCallbackUrl
import tech.ketc.numeri.infra.element.TlType
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.ui.model.delegate.*
import tech.ketc.numeri.util.arch.coroutine.ResponseDeferred
import tech.ketc.numeri.util.arch.coroutine.asyncResponse
import tech.ketc.numeri.util.arch.response.Response
import tech.ketc.numeri.util.arch.response.response
import javax.inject.Inject

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
        ITimelineInfoReader by TimelineInfoReader(mTimelineRepository) {

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
        fun TimelineInfo.toName(clientUsers: List<Pair<TwitterClient, TwitterUser>>): String {
            val clientUser = clientUsers.find { it.first.id == accountId }!!
            val client = clientUser.first
            val user = clientUser.second
            return when (type) {
                TlType.HOME -> "Home:${user.screenName}"
                TlType.MENTIONS -> "Mentions:${user.screenName}"
                TlType.USER_LIST -> "List:${client.twitter.showUserList(foreignId).name}"
                TlType.PUBLIC -> "User:${mUserRepository.show(client, foreignId).name}"
                TlType.FAVORITE -> "Favorite:${mUserRepository.show(client, foreignId).name}"
            }
        }
        return asyncResponse { infoList.map { it.toName(clientUsers) } }
    }
}