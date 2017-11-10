package tech.ketc.numeri.ui.model

import android.annotation.SuppressLint
import android.arch.lifecycle.*
import android.content.Intent
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.repository.IAccountRepository
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.ITwitterClient
import tech.ketc.numeri.domain.twitter.twitterCallbackUrl
import tech.ketc.numeri.ui.model.delegate.ClientHandler
import tech.ketc.numeri.ui.model.delegate.IClientHandler
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.ui.model.delegate.ImageLoadable
import tech.ketc.numeri.util.arch.BindingLifecycleAsyncTask
import tech.ketc.numeri.util.arch.livedata.AsyncLiveData
import tech.ketc.numeri.util.arch.response.Response
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
class MainViewModel @Inject constructor(private val app: App,
                                        private val accountRepository: IAccountRepository,
                                        private val userRepository: ITwitterUserRepository,
                                        private val imageRepository: IImageRepository)
    : ViewModel(), IImageLoadable by ImageLoadable(imageRepository),
        IClientHandler by ClientHandler(accountRepository, userRepository) {

    val latestUpdatedUser = userRepository.latestUpdatedLiveData

    fun createAuthorizationURL() = AsyncLiveData { accountRepository.createAuthorizationURL() }

    private fun createNewClientTask(oauthVerifier: String)
            = BindingLifecycleAsyncTask { accountRepository.createTwitterClient(oauthVerifier) }


    fun onNewIntent(intent: Intent, owner: LifecycleOwner, handle: (Response<ITwitterClient>) -> Unit) {
        val data = intent.data ?: return
        val oauthVerifier = data.getQueryParameter("oauth_verifier") ?: return
        if (!data.toString().startsWith(app.twitterCallbackUrl)) return
        createNewClientTask(oauthVerifier).run(owner, handle)
    }

}