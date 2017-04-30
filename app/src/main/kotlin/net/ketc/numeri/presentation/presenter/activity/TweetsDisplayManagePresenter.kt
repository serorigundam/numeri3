package net.ketc.numeri.presentation.presenter.activity

import android.os.Bundle
import net.ketc.numeri.domain.entity.*
import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.model.UserList
import net.ketc.numeri.domain.model.cache.userLists
import net.ketc.numeri.domain.model.cache.user
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.TweetsDisplayService
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.view.activity.TweetsDisplayManageActivityInterface
import net.ketc.numeri.util.rx.MySchedulers
import org.jetbrains.anko.toast
import java.util.*
import javax.inject.Inject

class TweetsDisplayManagePresenter(override val activity: TweetsDisplayManageActivityInterface)
    : AutoDisposablePresenter<TweetsDisplayManageActivityInterface>() {
    @Inject
    lateinit var oAuthService: OAuthService
    @Inject
    lateinit var displayService: TweetsDisplayService

    private val userListMap = HashMap<TwitterUser, List<UserList>>()
    private val userMap = HashMap<TwitterClient, TwitterUser>()
    private val userList = ArrayList<TwitterUser>()

    init {
        inject()
    }

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        singleTask(MySchedulers.twitter) {
            val clients = oAuthService.clients()
            clients.forEach {
                val user = it.user()
                userListMap.put(user, it.userLists())
                userMap.put(it, user)
                userList.add(user)
            }
        } error Throwable::printStackTrace success {
            setDisplays()
        }
    }

    private fun setDisplays() {
        fun TweetsDisplay.stringTo(clientUser: TwitterUser): String {
            return when (type) {
                TweetsDisplayType.HOME -> "Home:${clientUser.screenName}"
                TweetsDisplayType.MENTIONS -> "Mentions:${clientUser.screenName}"
                TweetsDisplayType.USER_LIST -> "List:${userListMap[clientUser]!!.find { it.id == foreignId }!!.name}"
                else -> throw InternalError()
            }
        }

        displayService.getDisplays(activity.group).forEach { display ->
            activity.add(display)
        }
        userList.forEach { user ->
            val displayPairs = ArrayList<Pair<TweetsDisplay, String>>()
            val client = userMap.map { it.key to it.value }.single { user == it.second }.first
            val token = client.toClientToken()
            val home = createTweetsDisplay(token, activity.group, -1, TweetsDisplayType.HOME)
            val clientUser = user
            displayPairs.add(home to home.stringTo(clientUser))
            val mentions = createTweetsDisplay(token, activity.group, -1, TweetsDisplayType.MENTIONS)
            displayPairs.add(mentions to mentions.stringTo(clientUser))
            userListMap[clientUser].orEmpty().forEach {
                val list = createTweetsDisplay(token, activity.group, it.id, TweetsDisplayType.USER_LIST)
                displayPairs.add(list to list.stringTo(clientUser))
            }
            activity.addDisplays(client to user, displayPairs)
        }
    }

    fun addDisplay(display: TweetsDisplay, text: String, client: TwitterClient) {
        val exist = displayService.getDisplays(activity.group).any {
            it.token.id == display.token.id
                    && it.foreignId == display.foreignId
                    && it.group == display.group
                    && it.type == display.type
        }
        if (exist) {
            ctx.toast("追加済み")
            return
        }

        val createdDisplay = displayService.createDisplay(activity.group, client, display.foreignId, display.type, text)
        activity.closeNavigation()
        activity.add(createdDisplay)
    }

    fun removeDisplay(display: TweetsDisplay) {
        displayService.remove(display)
        activity.remove(display)
    }

    fun replace(to: TweetsDisplay, by: TweetsDisplay) {
        displayService.replace(to, by)
        activity.replace(to, by)
    }

}
