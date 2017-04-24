package net.ketc.numeri.presentation.presenter.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import net.ketc.numeri.R
import net.ketc.numeri.domain.entity.TweetsDisplayGroup
import net.ketc.numeri.domain.entity.TweetsDisplayType
import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.model.cache.TwitterUserCache
import net.ketc.numeri.domain.model.cache.withUser
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.TweetsDisplayService
import net.ketc.numeri.presentation.view.activity.MainActivityInterface
import net.ketc.numeri.presentation.view.activity.TweetsDisplayGroupManageActivity
import net.ketc.numeri.util.log.v
import net.ketc.numeri.util.rx.MySchedulers
import net.ketc.numeri.util.rx.twitterThread
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*
import javax.inject.Inject

class MainPresenter(override val activity: MainActivityInterface) : AutoDisposablePresenter<MainActivityInterface>() {

    @Inject
    lateinit var oAuthService: OAuthService
    @Inject
    lateinit var tweetsDisplayService: TweetsDisplayService

    private val groups = ArrayList<TweetsDisplayGroup>()
    private var initialized = false

    init {
        inject()
    }

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        singleTask(MySchedulers.twitter) {
            oAuthService.clients().map { it.withUser() }
        }.error {
            ctx.toast(ctx.getString(R.string.authentication_failure))
            initialized = true
        }.success { pair ->
            if (pair.isEmpty()) {
                activity.showAddAccountDialog()
            }
            pair.map { it.second }
                    .forEach {
                        activity.addAccount(it, this)
                    }
            val client = pair.firstOrNull()?.first
            if (client != null) {
                val groups = tweetsDisplayService.getAllGroup()
                this.groups.addAll(groups)
                safePost {
                    groups.forEach {
                        activity.addGroup(it)
                    }
                    groups.firstOrNull()?.let { activity.showGroup(it) }
                }
            }
            initialized = true
        }
        startAccountsObserve()
    }

    override fun onResume() {
        super.onResume()

        if (!initialized) return
        applyDisplayGroupChanges()
    }

    private fun applyDisplayGroupChanges() {
        val newGroups = tweetsDisplayService.getAllGroup()
        var isChange = newGroups.size != groups.size

        if (!isChange) {
            newGroups.forEachIndexed { i, (id) ->
                if (i > groups.lastIndex || groups[i].id != id) {
                    isChange = true
                    return@forEachIndexed
                }
            }
        }
        if (isChange) {
            val added = newGroups.filter { (id) ->
                !groups.any { id == it.id }
            }
            val removed = groups.filter { (id) ->
                !newGroups.any { id == it.id }
            }

            added.forEach {
                activity.addGroup(it)
            }
            removed.forEach {
                activity.removeGroup(it)
            }
            v("MainActivity", "addedGroups:${added.joinToString { "$it" }}")
            v("MainActivity", "removedGroups:${removed.joinToString { "$it" }}")
            groups.clear()
            groups.addAll(newGroups)
            if (groups.singleOrNull { (id) -> activity.showingGroupId == id } == null) {
                if (groups.isNotEmpty()) activity.showGroup(groups.single())
            }
        }
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
            client.withUser()
        }.error {
            it.printStackTrace()
            ctx.toast(ctx.getString(R.string.authentication_failure))
        }.success {
            activity.addAccount(it.second, this)
            if (activity.accounts.size == 1) {
                val group = tweetsDisplayService.createGroup("メイン")
                tweetsDisplayService.createDisplay(group, it.first, -1L, TweetsDisplayType.HOME, "Home:${it.second.screenName}")
                applyDisplayGroupChanges()
            }
        }
    }

    fun startTweetsDisplayGroupManageActivity() {
        ctx.startActivity<TweetsDisplayGroupManageActivity>()
    }
}