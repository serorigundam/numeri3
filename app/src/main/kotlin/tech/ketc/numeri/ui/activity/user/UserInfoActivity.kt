package tech.ketc.numeri.ui.activity.user

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.jetbrains.anko.*
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.infra.element.TlType
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.ui.activity.media.MediaActivity
import tech.ketc.numeri.ui.fragment.timeline.TimelineFragment
import tech.ketc.numeri.ui.fragment.users.UsersFragment
import tech.ketc.numeri.ui.model.UserInfoViewModel
import tech.ketc.numeri.ui.model.UsersViewModel
import tech.ketc.numeri.ui.view.pager.ModifiablePagerAdapter
import tech.ketc.numeri.ui.view.pager.ModifiablePagerAdapter.Content
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.Updatable
import tech.ketc.numeri.util.android.*
import tech.ketc.numeri.util.android.ui.tab.SimpleOnTabSelectedListener
import tech.ketc.numeri.util.arch.lifecycle.IOnActiveRunner
import tech.ketc.numeri.util.arch.lifecycle.OnActiveRunner
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.nullable
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import tech.ketc.numeri.util.logTag
import java.io.Serializable
import javax.inject.Inject

class UserInfoActivity : AppCompatActivity(), AutoInject, IUserInfoUI by UserInfoUI(),
        HasSupportFragmentInjector, IOnActiveRunner by OnActiveRunner() {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var mAndroidInjector: DispatchingAndroidInjector<Fragment>

    private val mModel: UserInfoViewModel by viewModel { mViewModelFactory }

    private val mInfo by lazy { intent.getSerializableExtra(EXTRA_INFO) as Info }
    private val mClient: TwitterClient
        get() = mInfo.client
    private val mFollowButtonVisibleHandler = Handler()
    private val mTargetId: Long
        get() = mInfo.targetId

    private var mTitleIsVisible = false
    private var mIconIsVisible = true

    private val mPagerAdapter by lazy { ModifiablePagerAdapter<String, Fragment>(supportFragmentManager) }

    private var mPreviousAppBarOffset = -1

    private var mCurrentPagerPosition = 0
    private var mSwipeRefreshEnabled = false
    private var mIsAppBarExpanded = true


    companion object {
        private val EXTRA_INFO = "EXTRA_INFO"

        private val SAVED_CURRENT_PAGER_POSITION = "SAVED_CURRENT_PAGER_POSITION"
        private val SAVED_APPBAR_EXPANDED = "SAVED_APPBAR_EXPANDED"

        fun start(ctx: Context, client: TwitterClient, targetId: Long) {
            ctx.startActivity<UserInfoActivity>(EXTRA_INFO to Info(client, targetId))
        }

        fun start(ctx: Context, client: TwitterClient, target: TwitterUser) {
            ctx.startActivity<UserInfoActivity>(EXTRA_INFO to Info(client, target.id))
        }
    }

    override fun supportFragmentInjector() = mAndroidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOwner(this)
        setContentView(this)
        savedInstanceState?.run(this::restore)
        initializeUI()
        if (savedInstanceState == null) {
            toolbar.subtitle = " "
            toolbar.setTitleVisibility(false)
        }
        initialize()
    }

    private fun initializeUI() {
        initializeAppBar()
        setUpSupportActionbar(toolbar)
        toolbar.setFinishWithNavigationClick(this)
    }


    private fun initialize() {
        loadTwitterUser()
        initializePager()
    }


    private fun initializeAppBar() {
        appBar.addOnOffsetChangedListener { _, verticalOffset ->
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
                if (currentAppBarHeight == appBarHeight) {
                    //AppBar expansion
                    mIsAppBarExpanded = true
                    if (mSwipeRefreshEnabled)
                        swipeRefresh.isEnabled = true
                    Logger.v(logTag, "AppBarExpansion swipeRefreshable:${swipeRefresh.isEnabled}")
                }
            } else {
                if (currentAppBarHeight == appBarHeight) return@addOnOffsetChangedListener
                if (mIsAppBarExpanded) {
                    //AppBar collapse
                    mIsAppBarExpanded = false
                    swipeRefresh.isEnabled = false
                    Logger.v(logTag, "AppBar collapse  swipeRefreshable:${swipeRefresh.isEnabled}")
                }
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

    private fun initializePager() {
        val accountId = mClient.id
        val publicInfo = TimelineInfo(type = TlType.PUBLIC, accountId = accountId, foreignId = mTargetId)
        val favoriteInfo = TimelineInfo(type = TlType.FAVORITE, accountId = accountId, foreignId = mTargetId)
        val public = TimelineFragment.create(publicInfo, false)
        val favorite = TimelineFragment.create(favoriteInfo, false)
        val follows = UsersFragment.create(mClient, mTargetId, UsersViewModel.Type.FOLLOWS)
        val followers = UsersFragment.create(mClient, mTargetId, UsersViewModel.Type.FOLLOWERS)
        val contents = arrayListOf(Content("public", public, getString(R.string.tab_tweet)),
                Content("favorite", favorite, getString(R.string.tab_favorite)),
                Content("follows", follows, getString(R.string.tab_follow)),
                Content("followers", followers, getString(R.string.tab_follower)))
        runOnActive {
            pager.adapter = mPagerAdapter
            mPagerAdapter.setContents(contents)
            userInfoTab.setupWithViewPager(pager)
            pager.currentItem = mCurrentPagerPosition

            userInfoTab.addOnTabSelectedListener(object : SimpleOnTabSelectedListener() {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    mCurrentPagerPosition = tab.position
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    val fragment = mPagerAdapter.getContent(tab.position).fragment
                    when (fragment) {
                        is TimelineFragment -> fragment.scrollToTop()
                    }
                }
            })
            userInfoTab.visibility = View.VISIBLE
            mSwipeRefreshEnabled = true
            swipeRefresh.isEnabled = mIsAppBarExpanded
            swipeRefresh.setOnRefreshListener {
                val updatableList = contents.map { it.fragment as? Updatable }
                val updatableCount = updatableList.count()
                var completedCount = 0
                swipeRefresh.isRefreshing = true
                updatableList.forEach {
                    it?.update {
                        if (++completedCount == updatableCount)
                            swipeRefresh.isRefreshing = false
                    }
                }
            }
        }
    }

    private fun restore(savedState: Bundle) {
        mCurrentPagerPosition = savedState.getInt(SAVED_CURRENT_PAGER_POSITION)
        mIsAppBarExpanded = savedState.getBoolean(SAVED_APPBAR_EXPANDED)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SAVED_CURRENT_PAGER_POSITION, mCurrentPagerPosition)
        outState.putBoolean(SAVED_APPBAR_EXPANDED, mIsAppBarExpanded)
        super.onSaveInstanceState(outState)
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
        fun loadProfileImage(user: TwitterUser) {
            bindLaunch {
                val iconUrl = user.originalIconUrl
                val content = mModel.loadImage(iconUrl).await().nullable() ?: return@bindLaunch
                iconImage.setImageBitmap(content.bitmap)
                iconImage.setOnLongClickListener {
                    MediaActivity.start(this@UserInfoActivity, listOf(iconUrl), user.screenName)
                    true
                }
            }
        }

        fun loadHeader(user: TwitterUser) {
            bindLaunch {
                val content = mModel.loadHeaderImage(user)?.await()?.nullable() ?: return@bindLaunch
                headerImage.setImageBitmap(content.bitmap)
                headerImage.setOnLongClickListener {
                    val url = user.headerImageUrl ?: return@setOnLongClickListener true
                    MediaActivity.start(this@UserInfoActivity, listOf(url), user.screenName)
                    true
                }
            }
        }
        bindLaunch {
            val user = mModel.show(mClient, mTargetId).await().orError {
                toast(R.string.message_failed_user_info)
            } ?: return@bindLaunch
            loadProfileImage(user)
            loadHeader(user)
            supportActionBar!!.title = user.name
            supportActionBar!!.subtitle = user.screenName
            headerImage.backgroundColor = Color.parseColor("#${user.profileBackgroundColor}")
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
            appBar.setExpanded(mIsAppBarExpanded, false)
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