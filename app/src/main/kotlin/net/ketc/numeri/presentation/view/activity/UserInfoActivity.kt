package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.os.Bundle
import net.ketc.numeri.presentation.presenter.activity.UserInfoPresenter
import net.ketc.numeri.presentation.view.activity.ui.IUserInfoActivityUI
import net.ketc.numeri.presentation.view.activity.ui.UserInfoActivityUI
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity

class UserInfoActivity
    : ApplicationActivity<UserInfoPresenter>(),
        UserInfoActivityInterface,
        IUserInfoActivityUI by UserInfoActivityUI() {

    override val ctx: Context = this
    override val presenter = UserInfoPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        setSupportActionBar(toolbar)
        presenter.initialize(savedInstanceState)
    }

    companion object {
        fun start(ctx: Context) {
            ctx.startActivity<UserInfoActivity>()
        }
    }
}

interface UserInfoActivityInterface : ActivityInterface