package tech.ketc.numeri.ui.activity.user

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.*
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.ui.model.UserInfoViewModel
import tech.ketc.numeri.util.android.fadeIn
import tech.ketc.numeri.util.android.fadeOut
import tech.ketc.numeri.util.android.setFinishWithNavigationClick
import tech.ketc.numeri.util.android.setUpSupportActionbar
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.nullable
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import java.io.Serializable
import javax.inject.Inject

class UserInfoActivity : AppCompatActivity(), AutoInject, IUserInfoUI by UserInfoUI() {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: UserInfoViewModel by viewModel { mViewModelFactory }
    private var mPreviousAppBarOffset = -1
    private var mTitleIsVisible = false
    private var mIconIsVisible = true

    private val mInfo by lazy { intent.getSerializableExtra(EXTRA_INFO) as Info }
    private val mTargetId: Long
        get() = mInfo.targetId
    private val mClient: TwitterClient
        get() = mInfo.client

    companion object {
        private val EXTRA_INFO = "EXTRA_INFO"

        fun start(ctx: Context, client: TwitterClient, targetId: Long) {
            ctx.startActivity<UserInfoActivity>(EXTRA_INFO to Info(client, targetId))
        }

        fun start(ctx: Context, client: TwitterClient, target: TwitterUser) {
            ctx.startActivity<UserInfoActivity>(EXTRA_INFO to Info(client, target.id))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(this)
        initializeUI()
        initialize()
    }

    private fun initializeUI() {
        appBar.initialize()
        setUpSupportActionbar(toolbar)
        toolbar.setFinishWithNavigationClick(this)
    }

    private fun initialize() {
        setTwitterUser()
    }


    private fun AppBarLayout.initialize() {
        this.addOnOffsetChangedListener { _, verticalOffset ->
            val toolbarHeight = toolbar.height
            val appBarHeight = appBar.height
            if (mPreviousAppBarOffset == -1) {
                mPreviousAppBarOffset = appBarHeight - toolbarHeight
            }
            val plusOffset = -(-verticalOffset - (appBarHeight - toolbarHeight))
            val headerHeight = headerImage.height
            val currentAppBarHeight = plusOffset + toolbarHeight
            val down = mPreviousAppBarOffset < plusOffset
            val iconPoint = IntArray(2)
            iconRelative.getLocationInWindow(iconPoint)
            if (down) {
                if (currentAppBarHeight > headerHeight && mTitleIsVisible) {
                    mTitleIsVisible = false
                    toolbar.setTitleVisibility(false)
                    infoRelative.fadeIn()
                }
                if (iconPoint[1] > toolbarHeight && !mIconIsVisible) {
                    mIconIsVisible = true
                    iconRelative.fadeIn()
                }
                if (currentAppBarHeight == appBarHeight && !swipeRefresh.isEnabled) {
                    swipeRefresh.isEnabled = true
                }
            } else {
                if (swipeRefresh.isEnabled)
                    swipeRefresh.isEnabled = false
                if (currentAppBarHeight < headerHeight && !mTitleIsVisible) {
                    mTitleIsVisible = true
                    toolbar.setTitleVisibility(true)
                    infoRelative.fadeOut()
                }
                if (iconPoint[1] < toolbarHeight && mIconIsVisible) {
                    mIconIsVisible = false
                    iconRelative.fadeOut()
                }
            }
            mPreviousAppBarOffset = plusOffset
        }
    }

    private fun Toolbar.setTitleVisibility(isVisible: Boolean) {
        val executeAnimation: TextView.() -> Unit = if (isVisible) {
            { fadeIn() }
        } else {
            { fadeOut() }
        }
        this.forEachChild {
            if (it is TextView) {
                it.executeAnimation()
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setTwitterUser() {
        fun ImageView.setUrl(url: String) {
            bindLaunch {
                mModel.loadImage(url, false).await().nullable()?.let { (bitmap, _) ->
                    setImageBitmap(bitmap)
                }
            }
        }
        bindLaunch {
            val user = mModel.show(mClient, mTargetId).await().orError {
                toast(R.string.message_failed_user_info)
            } ?: return@bindLaunch
            supportActionBar!!.title = user.name
            supportActionBar!!.subtitle = user.screenName
            iconImage.setUrl(user.originalIconUrl)
            headerImage.backgroundColor = Color.parseColor("#${user.profileBackgroundColor}")
            user.headerImageUrl?.let { headerImage.setUrl(it) }
            protectedImage.visibility = if (user.isProtected) View.VISIBLE else View.INVISIBLE
            userNameText.text = user.name
            screenNameText.text = "@${user.screenName}"
            descriptionText.text = user.description
            locationText.text = "Location : ${user.location}"
            subInfoText.text = "following : ${user.friendsCount}" +
                    "\nfollowers : ${user.followersCount}" +
                    "\ntweets : ${user.statusesCount}" +
                    "\nfavorites : ${user.favoriteCount}"
            if (mClient.id == mTargetId) {
                relationInfoText.text = getString(R.string.myself)
            }
        }
    }

    data class Info(val client: TwitterClient, val targetId: Long) : Serializable
}