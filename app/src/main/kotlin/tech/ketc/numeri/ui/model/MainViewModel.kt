package tech.ketc.numeri.ui.model

import android.annotation.SuppressLint
import android.arch.lifecycle.*
import android.content.Intent
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.repository.IAccountRepository
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.domain.repository.ITwitterStreamRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.twitterCallbackUrl
import tech.ketc.numeri.ui.model.delegate.*
import tech.ketc.numeri.util.arch.BindingLifecycleAsyncTask
import tech.ketc.numeri.util.arch.livedata.AsyncLiveData
import tech.ketc.numeri.util.arch.response.Response
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
class MainViewModel @Inject constructor(private val app: App,
                                        private val accountRepository: IAccountRepository,
                                        userRepository: ITwitterUserRepository,
                                        imageRepository: IImageRepository,
                                        streamRepository: ITwitterStreamRepository)
    : ViewModel(),
        IImageLoadable by ImageLoadable(imageRepository),
        IClientHandler by ClientHandler(accountRepository, userRepository),
        IStreamHandler by StreamHandler(streamRepository),
        IUserHandler by UserHandler(userRepository) {

    fun createAuthorizationURL() = AsyncLiveData { accountRepository.createAuthorizationURL() }

    private fun createNewClientTask(oauthVerifier: String)
            = BindingLifecycleAsyncTask { accountRepository.createTwitterClient(oauthVerifier) }

    fun onNewIntent(intent: Intent, owner: LifecycleOwner, handle: (Response<TwitterClient>) -> Unit) {
        val data = intent.data ?: return
        val oauthVerifier = data.getQueryParameter("oauth_verifier") ?: return
        if (!data.toString().startsWith(app.twitterCallbackUrl)) return
        createNewClientTask(oauthVerifier).run(owner, handle)
    }
}