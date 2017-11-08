package tech.ketc.numeri.ui.model

import android.annotation.SuppressLint
import android.arch.lifecycle.*
import android.content.Intent
import android.net.Uri
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.IAccountRepository
import tech.ketc.numeri.domain.twitter.client.ITwitterClient
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.twitterCallbackUrl
import tech.ketc.numeri.util.arch.BindingLifecycleAsyncTask
import tech.ketc.numeri.util.arch.livedata.AsyncLiveData
import tech.ketc.numeri.util.arch.livedata.MutableAsyncLiveData
import tech.ketc.numeri.util.arch.livedata.asyncSwitchMap
import tech.ketc.numeri.util.arch.response.Response
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
class MainViewModel @Inject constructor(private val app: App, private val accountRepository: IAccountRepository) : ViewModel() {

    val clients = AsyncLiveData { accountRepository.clients() }


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