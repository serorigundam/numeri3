package net.ketc.numeri.presentation.presenter.activity

import android.os.Bundle
import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.model.cache.RelationType
import net.ketc.numeri.domain.model.cache.UserRelation
import net.ketc.numeri.domain.model.cache.convert
import net.ketc.numeri.domain.model.cache.showUser
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.view.activity.UserInfoActivityInterface
import net.ketc.numeri.util.rx.MySchedulers
import org.jetbrains.anko.toast
import javax.inject.Inject

class UserInfoPresenter(override val activity: UserInfoActivityInterface) : AutoDisposablePresenter<UserInfoActivityInterface>() {

    @Inject
    lateinit var oAuthService: OAuthService

    private var userRelation: UserRelation? = null
    private lateinit var client: TwitterClient

    init {
        inject()
    }

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        singleTask(MySchedulers.twitter) {
            oAuthService.clients().single { it.id == activity.twitterClientId }
        } error Throwable::printStackTrace success {
            safePost {
                client = it
                loadUser(it)
                activity.setClient(it)
                if (client.id != activity.targetUserId)
                    loadRelation(it, activity.targetUserId)
            }
        }
    }

    private fun loadUser(client: TwitterClient) {
        singleTask(MySchedulers.twitter) {
            client.showUser(activity.targetUserId)
        } error {
            it.printStackTrace()
            activity.ctx.toast("error")
        } success {
            activity.setTwitterUser(it)
        }
    }

    private fun loadRelation(client: TwitterClient, targetUserId: Long) {
        singleTask(MySchedulers.twitter) {
            client.twitter.showFriendship(client.id, targetUserId).convert()
        } error {
            it.printStackTrace()
            activity.ctx.toast("error")
        } success {
            activity.setUserRelation(it)
            userRelation = it
        }
    }

    fun relationUpdate() {
        userRelation?.let {
            activity.followButtonIsEnabled = false
            singleTask(MySchedulers.twitter) {
                val twitter = client.twitter
                val targetUserId = activity.targetUserId
                when (it.type) {
                    RelationType.MUTUAL, RelationType.FOLLOWING -> twitter.destroyFriendship(targetUserId)
                    RelationType.FOLLOWED, RelationType.NOTHING -> twitter.createFriendship(targetUserId)
                    else -> throw IllegalStateException()
                }
                twitter.showFriendship(client.id, targetUserId).convert()
            } error {
                it.printStackTrace()
                activity.ctx.toast("失敗しました")
                activity.followButtonIsEnabled = true
            } success {
                activity.setUserRelation(it)
                activity.followButtonIsEnabled = true
            }
        }
    }
}