package net.ketc.numeri.presentation.presenter.activity

import android.content.Intent
import android.net.Uri
import net.ketc.numeri.R
import net.ketc.numeri.domain.entity.TweetsDisplayType
import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.model.cache.TwitterUserCache
import net.ketc.numeri.domain.model.cache.convertAndCacheOrGet
import net.ketc.numeri.domain.model.cache.withUser
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.TweetsDisplayService
import net.ketc.numeri.presentation.view.activity.MainActivityInterface
import net.ketc.numeri.util.rx.MySchedulers
import net.ketc.numeri.util.rx.twitterThread
import org.jetbrains.anko.toast
import javax.inject.Inject

class MainPresenter(override val activity: MainActivityInterface) : AutoDisposablePresenter<MainActivityInterface>() {

    @Inject
    lateinit var oAuthService: OAuthService
    @Inject
    lateinit var tweetsDisplayService: TweetsDisplayService

    init {
        inject()
    }

    override fun initialize() {
        super.initialize()
        singleTask(MySchedulers.twitter) {
            oAuthService.clients().map { it.withUser() }
        }.error {
            ctx.toast(ctx.getString(R.string.authentication_failure))
        }.success { pair ->
            pair.map { it.second }
                    .forEach {
                        activity.addAccount(it, this)
                    }
            //todo 仮置き
            val client = pair.firstOrNull()?.first
            if (client != null) {
                if (tweetsDisplayService.getAllGroup().isEmpty()) {
                    val group = tweetsDisplayService.createGroup()
                    tweetsDisplayService.createDisplay(group, client, -1, TweetsDisplayType.HOME)
                }
                val group = tweetsDisplayService.getAllGroup().first()
                val displays = tweetsDisplayService.getDisplays(group)
                activity.setDisplay(displays.first())
            }
        }
        startAccountsObserve()
    }

    fun startAccountsObserve() {
        val accounts = activity.accounts
        TwitterUserCache.userUpdateFlowable
                .onBackpressureBuffer()
                .twitterThread()
                .filter { user -> accounts.any { it == user } }
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
            ctx.toast(ctx.getString(R.string.failed_generate_authentication_url))
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
            client.twitter.showUser(client.id).convertAndCacheOrGet()
        }.error {
            it.printStackTrace()
            ctx.toast(ctx.getString(R.string.authentication_failure))
        }.success {
            activity.addAccount(it, this)
        }
    }
}