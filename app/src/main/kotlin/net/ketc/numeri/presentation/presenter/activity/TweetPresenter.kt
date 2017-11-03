package net.ketc.numeri.presentation.presenter.activity

import android.app.Service
import android.content.Intent
import net.ketc.numeri.presentation.view.activity.TweetActivityInterface
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import net.ketc.numeri.domain.android.service.ITweetService
import net.ketc.numeri.domain.android.service.TweetService
import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.model.cache.withUser
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.util.rx.MySchedulers
import org.jetbrains.anko.toast
import javax.inject.Inject


class TweetPresenter(override val activity: TweetActivityInterface)
    : AutoDisposablePresenter<TweetActivityInterface>() {

    @Inject
    lateinit var oAuthService: OAuthService

    private var clients: List<Pair<TwitterClient, TwitterUser>> = emptyList()
    private lateinit var tweetService: ITweetService
    private var clientId: Long = -1
    private val mentionRegexList = ArrayList<Regex>()
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            service ?: return
            tweetService = (service as ITweetService.Binder).getService()
            activity.isSendTweetButtonEnabled = true
        }
    }

    init {
        inject()
    }

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        singleTask(MySchedulers.twitter) {
            oAuthService.clients().map { it.withUser() }
        } error {
            ctx.toast("error")
        } success {
            clients = it
            initializeInternal(savedInstanceState)
            val intent = Intent(ctx, TweetService::class.java)
            ctx.startService(intent)
            ctx.bindService(intent,
                    connection, Service.BIND_AUTO_CREATE)
        }
    }

    private fun initializeInternal(savedInstanceState: Bundle?) {
        val id = activity.defaultClientId
        val user = clients.firstOrNull { it.second.id == id }?.second
                ?: clients.firstOrNull()?.second ?: throw IllegalArgumentException()
        setTweetUser(user.id)
        if (savedInstanceState == null) {
            setReplyToScreenName(user)
        }
    }

    private fun setReplyToScreenName(user: TwitterUser) {
        val replyToStatus = activity.replyToStatus ?: return

        val replyToScreenName = "@" + replyToStatus.user.screenName
        val mentionEntities = replyToStatus.userMentionEntities
        val mentions = mentionEntities.filter { it.screenName != user.screenName }.joinToString { "@${it.screenName} " }
        activity.setReplyInfo("reply to $replyToScreenName")
        val text = activity.text
        activity.text = "$replyToScreenName $mentions $text "
        mentionRegexList.addAll(mentionEntities.map { "@$it ?".toRegex() })
        mentionRegexList.add("$replyToScreenName ?".toRegex())
    }

    fun sendTweet() {
        val clientPair = clients.firstOrNull { it.first.id == clientId } ?: throw IllegalArgumentException()
        val client = clientPair.first
        val clientUser = clientPair.second
        val replyToStatusId = activity.replyToStatus?.id
        tweetService.sendTweet(client, clientUser, activity.text, replyToStatusId, activity.mediaList)
        activity.clear()
        activity.finish()
    }

    fun setTweetUser(clientId: Long) {
        val user = clients.map { it.second }.firstOrNull { it.id == clientId }
                ?: throw IllegalArgumentException()
        activity.setUserIcon(user)
        this.clientId = clientId
    }

    fun onClickSelectUserButton() {
        clients.takeIf { it.isNotEmpty() } ?: return
        activity.showSelectUserDialog(clients.map { it.second })
    }

    fun checkRemaining() {
        val remaining = activity.text.remaining()
        activity.setRemaining(remaining)
    }

    private fun String.remaining(): Int {
        var s = this
//        mentionRegexList.forEach {
//            s = s.replace(it, "")
//        }//todo 140文字以上ツイートに要対応
        return SENDABLE_TEXT_COUNT - s.count()
    }

    override fun onDestroy() {
        super.onDestroy()
        ctx.unbindService(connection)
    }

    companion object {
        val SENDABLE_TEXT_COUNT = 140
    }
}