package net.ketc.numeri.presentation.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import net.ketc.numeri.R
import net.ketc.numeri.domain.entity.TweetsDisplayGroup
import net.ketc.numeri.domain.entity.TweetsDisplayType
import net.ketc.numeri.domain.entity.createTweetsDisplay
import net.ketc.numeri.domain.entity.toClientToken
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.model.RelationType
import net.ketc.numeri.domain.model.UserRelation
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.activity.UserInfoPresenter
import net.ketc.numeri.presentation.view.Refreshable
import net.ketc.numeri.presentation.view.SimplePagerContent
import net.ketc.numeri.presentation.view.activity.ui.IUserInfoActivityUI
import net.ketc.numeri.presentation.view.activity.ui.UserInfoActivityUI
import net.ketc.numeri.presentation.view.component.adapter.SimplePagerAdapter
import net.ketc.numeri.presentation.view.component.ui.TwitterUserViewUI
import net.ketc.numeri.presentation.view.fragment.TimeLineFragment
import net.ketc.numeri.presentation.view.fragment.UsersFragment
import net.ketc.numeri.presentation.view.fragment.UsersFragmentInterface
import net.ketc.numeri.util.android.download
import net.ketc.numeri.util.android.fadeIn
import net.ketc.numeri.util.android.fadeOut
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.nestedScrollView

class UserInfoActivity
    : ApplicationActivity<UserInfoPresenter>(),
        UserInfoActivityInterface,
        IUserInfoActivityUI by UserInfoActivityUI() {
    override val twitterClientId: Long by lazy { intent.getLongExtra(EXTRA_CLIENT_ID, -1L).takeIf { it != -1L } ?: throw IllegalStateException() }
    override val targetUserId: Long by lazy { intent.getLongExtra(EXTRA_TARGET_USER_ID, -1L).takeIf { it != -1L } ?: throw IllegalStateException() }
    override val ctx: Context = this
    override val presenter = UserInfoPresenter(this)
    override var isRefreshing: Boolean
        get() = swipeRefresh.isRefreshing
        set(value) {
            swipeRefresh.isRefreshing = value
        }
    override var followButtonIsEnabled: Boolean
        get() = followButton.isClickable
        set(value) {
            followButton.isClickable = value
        }
    private var previousAppBarOffset = -1
    private var titleIsVisible = false
    private var iconIsVisible = true


    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab) {
            val fragment = (pager.adapter as? SimplePagerAdapter)?.getItem(tab.position)
            (fragment as? TimeLineFragment)?.scrollToTop()
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
        }

        override fun onTabSelected(tab: TabLayout.Tab) {
        }
    }

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
                if (currentAppBarHeight == appBarHeight && !swipeRefresh.isEnabled) {
                    swipeRefresh.isEnabled = true
                }
            } else {
                if (swipeRefresh.isEnabled)
                    swipeRefresh.isEnabled = false
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

    @SuppressLint("SetTextI18n")
    override fun setTwitterUser(user: TwitterUser) {
        supportActionBar!!.title = user.name
        supportActionBar!!.subtitle = user.screenName
        iconImage.download(user.originalIconUrl, presenter, false, success = { iconImage.fadeIn().execute() })
        headerImage.backgroundColor = Color.parseColor("#${user.profileBackgroundColor}")
        user.headerImageUrl?.let {
            headerImage.download(it, presenter, false, success = { headerImage.fadeIn().execute() })
        }
        protectedImage.visibility = if (user.isProtected) View.VISIBLE else View.INVISIBLE
        userNameText.text = user.name
        screenNameText.text = "@${user.screenName}"
        descriptionText.text = user.description
        locationText.text = "Location : ${user.location}"
        subInfoText.text = "following : ${user.friendsCount}" +
                "\nfollowers : ${user.followersCount}" +
                "\ntweets : ${user.statusesCount}" +
                "\nfavorites : ${user.favoriteCount}"
        if (twitterClientId == targetUserId) {
            relationInfoText.text = getString(R.string.myself)
        }
    }

    override fun setClient(client: TwitterClient) {
        pager.initialize(client)
        swipeRefresh.setOnRefreshListener {
            val refreshableList = pager.components.filter { it is Refreshable }
                    .map { it as Refreshable }
            var refreshedCount = 0
            isRefreshing = true
            refreshableList.forEach {
                it.refresh {
                    refreshedCount++
                    if (refreshedCount == refreshableList.size) {
                        isRefreshing = false
                    }
                }
            }
        }
    }

    override fun setUserRelation(userRelation: UserRelation) {
        setRelationText(userRelation)
        setFollowButtonState(userRelation)
    }

    private fun setRelationText(userRelation: UserRelation) = when (userRelation.type) {
        RelationType.FOLLOWED, RelationType.MUTUAL ->
            relationInfoText.text = getString(R.string.following_you)
        RelationType.BLOCkING ->
            relationInfoText.text = getString(R.string.blocking)
        else -> {
            relationInfoText.text = ""
        }
    }

    private val followButtonVisibleHandler = Handler()
    private val followButtonVisibleListener: () -> Unit = {
        followButton.fadeIn().end { visibility = View.VISIBLE }.execute()
    }

    private fun setFollowButtonState(userRelation: UserRelation) {
        fun anim(id: Int) {
            val drawable = ctx.getDrawable(id)
            val animatable = drawable as Animatable
            followButton.setImageDrawable(drawable)
            animatable.start()
            if (followButton.visibility != View.VISIBLE) {
                followButtonVisibleHandler.postDelayed(followButtonVisibleListener, 300)
            }
        }
        when (userRelation.type) {
            RelationType.FOLLOWING, RelationType.MUTUAL -> {
                anim(R.drawable.vector_anim_person)
            }
            RelationType.FOLLOWED, RelationType.NOTHING -> {
                anim(R.drawable.vector_anim_person_add)
            }
            else -> {
                followButton.visibility = View.INVISIBLE
                followButton.fadeOut().execute()
            }
        }
        followButton.setOnClickListener { presenter.relationUpdate() }
    }

    override fun onDestroy() {
        super.onDestroy()
        followButtonVisibleHandler.removeCallbacks(followButtonVisibleListener)
    }

    private fun ViewPager.initialize(client: TwitterClient) {
        fun createFragment(type: TweetsDisplayType, name: String): Fragment {
            return TimeLineFragment.create(createTweetsDisplay(client.toClientToken(),
                    TweetsDisplayGroup(), targetUserId, type, name), false)
        }

        val fragments = ArrayList<Fragment>().apply {
            add(createFragment(TweetsDisplayType.PUBLIC, ctx.getString(R.string.tab_tweet)))
            add(createFragment(TweetsDisplayType.FAVORITE, ctx.getString(R.string.tab_favorite)))
            add(createFragment(TweetsDisplayType.MEDIA, ctx.getString(R.string.tab_media)))
            add(UsersFragment.create(client.id, targetUserId, UsersFragmentInterface.Type.FRIENDS))
            add(UsersFragment.create(client.id, targetUserId, UsersFragmentInterface.Type.FOLLOWERS))
        }
        this.adapter = SimplePagerAdapter(supportFragmentManager, fragments)
        userProfileTabLayout.setupWithViewPager(this)
        userProfileTabLayout.addOnTabSelectedListener(onTabSelectedListener)
    }

    private val ViewPager.components: List<Fragment>
        get() = (adapter as SimplePagerAdapter).itemList


    class EmptyFragment : Fragment(), SimplePagerContent {
        override val contentName: String = "empty"
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            return TwitterUserViewUI(context).createView()

        }
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
    var isRefreshing: Boolean
    var followButtonIsEnabled: Boolean
    fun setTwitterUser(user: TwitterUser)
    fun setClient(client: TwitterClient)
    fun setUserRelation(userRelation: UserRelation)
}