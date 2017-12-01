package tech.ketc.numeri.ui.activity.user

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
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
import tech.ketc.numeri.domain.twitter.model.getIconUrl
import tech.ketc.numeri.ui.model.UserInfoViewModel
import tech.ketc.numeri.util.android.*
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

    private val mFollowButtonVisibleHandler = Handler()

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
        loadTwitterUser()
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
    private fun loadTwitterUser() {
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
            iconImage.setUrl(user.getIconUrl(true))
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
            } else {
                loadRelation()
            }
        }
    }

    private fun visibleFollowButton() {
        followButton.visibility = View.VISIBLE
        followButton.fadeIn()
    }


    private fun loadRelation() {
        bindLaunch {
            val relation = mModel.loadRelation(mClient, mTargetId).await().orError {
                toast(R.string.message_failed_get_relation)
            } ?: return@bindLaunch
            setRelation(relation)
        }
    }

    private fun setRelation(relation: UserInfoViewModel.IRelation) {
        fun animate(id: Int) {
            val drawable = ctx.getDrawable(id)
            val animatable = drawable as Animatable
            followButton.setImageDrawable(drawable)
            animatable.start()
            if (followButton.visibility != View.VISIBLE) {
                mFollowButtonVisibleHandler.postDelayed(this::visibleFollowButton, 300)
            }
        }

        fun hide() {
            followButton.visibility = View.INVISIBLE
            followButton.fadeOut()
            relationInfoText.text = ""
            followInfoText.text = ""
        }

        when {
            relation.isBlocking -> hide()
            relation.isFollowing -> animate(R.drawable.vector_anim_person)
            !relation.isFollowing -> animate(R.drawable.vector_anim_person_add)
            else -> hide()
        }

        fun mutual() {
            relationInfoText.text = getString(R.string.mutual)
        }

        fun oneSidedFollowing() {
            relationInfoText.text = getString(R.string.one_sided_following)
        }

        fun oneSidedFollowed() {
            relationInfoText.text = getString(R.string.one_sided_followed)
        }

        when {
            relation.isMutual -> mutual()
            relation.isFollowing && !relation.isFollowed -> oneSidedFollowing()
            !relation.isFollowing && relation.isFollowed -> oneSidedFollowed()
        }
        when {
            relation.isFollowing -> followInfoText.text = getString(R.string.following)
            !relation.isFollowing -> followInfoText.text = getString(R.string.un_following)
        }
        followInfoText.fadeIn()
        followButton.setOnClickListener { onClickFollowButton(relation) }
    }

    private fun onClickFollowButton(relation: UserInfoViewModel.IRelation) {
        followButton.isClickable = false
        bindLaunch {
            val r = mModel.updateRelation(relation, mClient, mTargetId).await().orError {
                toast(R.string.failed_update_relation)
            } ?: return@bindLaunch
            setRelation(r)
            followButton.isClickable = true
        }
    }

    override fun onDestroy() {
        mFollowButtonVisibleHandler.removeCallbacks(this::visibleFollowButton)
        super.onDestroy()
    }

    data class Info(val client: TwitterClient, val targetId: Long) : Serializable
}