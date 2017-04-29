package net.ketc.numeri.presentation.view.activity

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.presentation.presenter.activity.UserInfoPresenter
import net.ketc.numeri.presentation.view.activity.ui.IUserInfoActivityUI
import net.ketc.numeri.presentation.view.activity.ui.UserInfoActivityUI
import net.ketc.numeri.util.android.download
import net.ketc.numeri.util.android.fadeIn
import net.ketc.numeri.util.android.fadeOut
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.forEachChild
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity

class UserInfoActivity
    : ApplicationActivity<UserInfoPresenter>(),
        UserInfoActivityInterface,
        IUserInfoActivityUI by UserInfoActivityUI() {
    override val twitterClientId: Long by lazy { intent.getLongExtra(EXTRA_CLIENT_ID, -1L).takeIf { it != -1L } ?: throw IllegalStateException() }
    override val targetUserId: Long by lazy { intent.getLongExtra(EXTRA_TARGET_USER_ID, -1L).takeIf { it != -1L } ?: throw IllegalStateException() }
    override val ctx: Context = this
    override val presenter = UserInfoPresenter(this)
    private var previousAppBarOffset = -1
    private var titleIsVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        toolbar.subtitle = "  "
        toolbar.forEachChild { if (it is TextView) it.visibility = View.INVISIBLE } //hide the title
        appBar.initialize()
        presenter.initialize(savedInstanceState)
    }

    private var iconIsVisible = true
    private fun AppBarLayout.initialize() {
        this.addOnOffsetChangedListener { _, verticalOffset ->
            val toolbarHeight = toolbar.height
            val appBarHeight = appBar.height
            if (previousAppBarOffset == -1) {
                previousAppBarOffset = appBarHeight - toolbarHeight
            }
            val plusOffset = -(-verticalOffset - (appBarHeight - toolbarHeight))
            val headerHeight = headerImage.height
            val currentAppBarHeight = plusOffset + toolbarHeight
            val down = previousAppBarOffset < plusOffset
            val iconPoint = IntArray(2)
            iconRelative.getLocationInWindow(iconPoint)
            if (down) {
                if (currentAppBarHeight > headerHeight && titleIsVisible) {
                    titleIsVisible = false
                    toolbar.setTitleVisibility(false)
                    infoRelative.fadeIn().end { visibility = View.VISIBLE }.execute()
                }
                if (iconPoint[1] > toolbarHeight && !iconIsVisible) {
                    iconIsVisible = true
                    iconRelative.fadeIn().end { visibility = View.VISIBLE }.execute()
                }
            } else {
                if (currentAppBarHeight < headerHeight && !titleIsVisible) {
                    titleIsVisible = true
                    toolbar.setTitleVisibility(true)
                    infoRelative.fadeOut().end { visibility = View.INVISIBLE }.execute()
                }
                if (iconPoint[1] < toolbarHeight && iconIsVisible) {
                    iconIsVisible = false
                    iconRelative.fadeOut().end { visibility = View.INVISIBLE }.execute()
                }
            }
            previousAppBarOffset = plusOffset
        }
    }

    private fun Toolbar.setTitleVisibility(isVisible: Boolean) {
        val executeAnimation: TextView.() -> Unit = if (isVisible) {
            { fadeIn().end { visibility = View.VISIBLE }.execute() }
        } else {
            { fadeOut().end { visibility = View.INVISIBLE }.execute() }
        }
        this.forEachChild {
            if (it is TextView) {
                it.executeAnimation()
            }
        }
    }

    override fun setTwitterUser(user: TwitterUser) {
        supportActionBar!!.title = user.name
        supportActionBar!!.subtitle = user.screenName
        iconImage.download(user.originalIconUrl, presenter, false, success = { iconImage.fadeIn().execute() })
        headerImage.backgroundColor = Color.parseColor("#${user.profileBackgroundColor}")
        user.headerImageUrl?.let {
            headerImage.download(it, presenter, false, success = { headerImage.fadeIn().execute() })
        }
        userNameText.text = user.name
        screenNameText.text = user.screenName
        descriptionText.text = user.description
        locationText.text = "Location : ${user.location}"
        subInfoText.text = "following : ${user.friendsCount}" +
                "\nfollowers : ${user.followersCount}" +
                "\ntweets : ${user.statusesCount}" +
                "\nfavorites : ${user.favoriteCount}"
    }

    companion object {
        fun start(ctx: Context, clientId: Long, targetUserId: Long) {
            ctx.startActivity<UserInfoActivity>(EXTRA_CLIENT_ID to clientId,
                    EXTRA_TARGET_USER_ID to targetUserId)
        }

        val EXTRA_CLIENT_ID = "EXTRA_CLIENT_ID"
        val EXTRA_TARGET_USER_ID = "EXTRA_TARGET_USER_ID"
    }
}

interface UserInfoActivityInterface : ActivityInterface {
    val twitterClientId: Long
    val targetUserId: Long
    fun setTwitterUser(user: TwitterUser)
}