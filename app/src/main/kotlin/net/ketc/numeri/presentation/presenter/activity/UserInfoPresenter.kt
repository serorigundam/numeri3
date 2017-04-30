package net.ketc.numeri.presentation.presenter.activity

import android.os.Bundle
import net.ketc.numeri.domain.inject
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


    init {
        inject()
    }

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        singleTask(MySchedulers.twitter) {
            oAuthService.clients().single { it.id == activity.twitterClientId }
        } error Throwable::printStackTrace success {
            loadUser(it)
            activity.setClient(it)
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

}