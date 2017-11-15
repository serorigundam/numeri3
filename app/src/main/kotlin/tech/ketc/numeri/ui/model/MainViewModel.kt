package tech.ketc.numeri.ui.model

import android.annotation.SuppressLint
import android.arch.lifecycle.*
import android.content.Intent
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.repository.*
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.domain.twitter.twitterCallbackUrl
import tech.ketc.numeri.infra.element.TlType
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.ui.model.delegate.*
import tech.ketc.numeri.util.arch.BindingLifecycleAsyncTask
import tech.ketc.numeri.util.arch.livedata.AsyncLiveData
import tech.ketc.numeri.util.arch.response.Response
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
class MainViewModel @Inject constructor(private val app: App,
                                        private val accountRepository: IAccountRepository,
                                        private val timelineRepository: ITimelineInfoRepository,
                                        private val userRepository: ITwitterUserRepository,
                                        imageRepository: IImageRepository,
                                        streamRepository: ITwitterStreamRepository)
    : ViewModel(),
        IImageLoadable by ImageLoadable(imageRepository),
        IClientHandler by ClientHandler(accountRepository, userRepository),
        IStreamHandler by StreamHandler(streamRepository),
        IUserHandler by UserHandler(userRepository),
        ITimelineChangeObserver by TimelineChangeObserver(timelineRepository) {

    val groupList: AsyncLiveData<List<TimelineGroup>>
        get() = AsyncLiveData { timelineRepository.getGroupList() }

    fun createAuthorizationURL() = AsyncLiveData { accountRepository.createAuthorizationURL() }

    private fun createNewClientTask(oauthVerifier: String)
            = BindingLifecycleAsyncTask { accountRepository.createTwitterClient(oauthVerifier) }

    fun onNewIntent(intent: Intent, owner: LifecycleOwner, handle: (Response<TwitterClient>) -> Unit) {
        val data = intent.data ?: return
        val oauthVerifier = data.getQueryParameter("oauth_verifier") ?: return
        if (!data.toString().startsWith(app.twitterCallbackUrl)) return
        createNewClientTask(oauthVerifier).run(owner, handle)
    }

    fun loadTimelineInfoList(owner: LifecycleOwner, groupName: String, handle: (List<TimelineInfo>) -> Unit) = BindingLifecycleAsyncTask {
        timelineRepository.selectByGroup(TimelineGroup(groupName))
    }.run(owner) {
        it.ifPresent { handle(it) }
        it.ifError { it.printStackTrace() }
    }

    fun createNameList(owner: LifecycleOwner, clientUsers: List<Pair<TwitterClient, TwitterUser>>,
                       infoList: List<TimelineInfo>,
                       handle: (List<String>) -> Unit) = BindingLifecycleAsyncTask {

        fun TimelineInfo.toName(clientUsers: List<Pair<TwitterClient, TwitterUser>>): String {
            val clientUser = clientUsers.find { it.first.id == accountId }!!
            val client = clientUser.first
            val user = clientUser.second
            return when (type) {
                TlType.HOME -> "Home:${user.screenName}"
                TlType.MENTIONS -> "Mentions:${user.screenName}"
                TlType.USER_LIST -> "List:${client.twitter.showUserList(foreignId).name}"
                TlType.PUBLIC -> "User:${userRepository.show(client, foreignId).name}"
                TlType.FAVORITE -> "Favorite:${userRepository.show(client, foreignId).name}"
            }
        }
        infoList.map { it.toName(clientUsers) }
    }.run(owner) {
        it.ifPresent(handle)
        it.ifError { it.printStackTrace() }
    }
}