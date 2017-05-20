package net.ketc.numeri.presentation.presenter.activity

import android.os.Bundle
import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.model.RelationType
import net.ketc.numeri.domain.model.UserRelation
import net.ketc.numeri.domain.model.cache.convert
import net.ketc.numeri.domain.model.cache.showUser
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.view.activity.UserInfoActivityInterface
import net.ketc.numeri.util.rx.MySchedulers
import org.jetbrains.anko.toast
import javax.inject.Inject

object UserInfoPresenterFactory : PresenterFactory<UserInfoPresenter>() {
    override fun create() = UserInfoPresenter()
}

class UserInfoPresenter : AutoDisposablePresenter<UserInfoActivityInterface>() {

    @Inject
    lateinit var oAuthService: OAuthService

    private var userRelation: UserRelation? = null
    private lateinit var client: TwitterClient

    init {
        inject()
    }

    override fun initialize(savedInstanceState: Bundle?, isStartedForFirst: Boolean) {
        super.initialize(savedInstanceState, isStartedForFirst)
        singleTask(MySchedulers.twitter) {
            oAuthService.clients().single { it.id == activity.twitterClientId }
        } error Throwable::printStackTrace safeSuccess { result ->
            client = result
            loadUser(result)
            this.setClient(result)
            if (client.id != activity.targetUserId)
                loadRelation(result, activity.targetUserId)
        }
    }

    private fun loadUser(client: TwitterClient) {
        singleTask(MySchedulers.twitter) {
            client.showUser(activity.targetUserId)
        } safeError {
            it.printStackTrace()
            this.ctx.toast("error")
        } safeSuccess { result ->
            this.setTwitterUser(result)
        }
    }

    private fun loadRelation(client: TwitterClient, targetUserId: Long) {
        singleTask(MySchedulers.twitter) {
            client.twitter.showFriendship(client.id, targetUserId).convert()
        } safeError {
            it.printStackTrace()
            this.ctx.toast("error")
        } safeSuccess { result ->
            this.setUserRelation(result)
            userRelation = result
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
            } safeError {
                it.printStackTrace()
                this.ctx.toast("失敗しました")
                this.followButtonIsEnabled = true
            } safeSuccess { result ->
                this.setUserRelation(result)
                followButtonIsEnabled = true
            }
        }
    }
}