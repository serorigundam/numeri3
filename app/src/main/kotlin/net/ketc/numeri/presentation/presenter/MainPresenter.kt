package net.ketc.numeri.presentation.presenter

import android.content.Intent
import android.net.Uri
import net.ketc.numeri.R
import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.model.cache.TwitterUserCache
import net.ketc.numeri.domain.model.cache.convertAndCache
import net.ketc.numeri.domain.model.cache.withUser
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.presentation.view.MainActivityInterface
import net.ketc.numeri.util.rx.MySchedulers
import net.ketc.numeri.util.rx.twitterThread
import org.jetbrains.anko.toast
import javax.inject.Inject

class MainPresenter(override val activity: MainActivityInterface) : AutoDisposablePresenter<MainActivityInterface>() {

    @Inject
    lateinit var oAuthService: OAuthService

    init {
        inject()
    }

    override fun initialize() {
        super.initialize()
        singleTask(MySchedulers.twitter) {
            oAuthService.clients().map { it.withUser() }
        }.error {
            ctx.toast("認証失敗")
        }.success { pair ->
            pair.map { it.second }
                    .forEach { activity.addAccount(it) }
        }

        TwitterUserCache.userUpdateFlowable
                .onBackpressureBuffer()
                .twitterThread()
                .subscribe({
                    activity.updateAccount(it)
                }).autoDispose()
    }

    fun newAuthenticate() {
        activity.addAccountButtonEnabled = false
        singleTask(MySchedulers.twitter) {
            val authorizationURL = oAuthService.createAuthorizationURL()
            val uri = Uri.parse(authorizationURL)
            Intent(Intent.ACTION_VIEW, uri)
        }.error {
            activity.addAccountButtonEnabled = true
            it.printStackTrace()
            ctx.toast("認証用URLの生成に失敗")
        }.success {
            activity.addAccountButtonEnabled = true
            ctx.startActivity(it)
        }
    }

    fun onNewIntent(intent: Intent) {
        val data: Uri? = intent.data
        val oauthVerifier = intent.data?.getQueryParameter("oauth_verifier")
        val scheme = ctx.getString(R.string.twitter_callback_scheme)
        val host = ctx.getString(R.string.twitter_callback_host)
        if (oauthVerifier == null || !(data?.toString()?.startsWith("$scheme://$host") ?: true))
            return
        singleTask(MySchedulers.twitter) {
            val client = oAuthService.createTwitterClient(oauthVerifier)
            client.twitter.showUser(client.id).convertAndCache()
        }.error {
            it.printStackTrace()
            ctx.toast("認証用URLの生成に失敗")
        }.success {
            activity.addAccount(it)
        }
    }

}