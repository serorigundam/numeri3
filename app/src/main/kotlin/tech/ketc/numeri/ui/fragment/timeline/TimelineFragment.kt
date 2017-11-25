package tech.ketc.numeri.ui.fragment.timeline

import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.MediaEntity
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.UrlEntity
import tech.ketc.numeri.domain.twitter.model.link
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.ui.components.ISwipeRefreshRecyclerUIComponent
import tech.ketc.numeri.ui.components.SwipeRefreshRecyclerUIComponent
import tech.ketc.numeri.ui.components.createBottomSheetUIComponent
import tech.ketc.numeri.ui.components.createMenuItemUIComponent
import tech.ketc.numeri.ui.model.TimeLineViewModel
import tech.ketc.numeri.ui.view.recycler.timeline.TimeLineDataSourceAdapter
import tech.ketc.numeri.ui.view.recycler.timeline.TweetViewHolder
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.arg
import tech.ketc.numeri.util.arch.viewmodel.viewModel
import tech.ketc.numeri.util.di.AutoInject
import javax.inject.Inject
import tech.ketc.numeri.util.android.act
import tech.ketc.numeri.util.android.pref
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.logTag
import java.util.ArrayList

class TimelineFragment : Fragment(), AutoInject, ISwipeRefreshRecyclerUIComponent by SwipeRefreshRecyclerUIComponent() {

    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mModel: TimeLineViewModel by viewModel { mViewModelFactory }

    private val mTlInfo by lazy { arg.getSerializable(EXTRA_TIMELINE_INFO) as TimelineInfo }
    private var mSavedTopChildAdapterPosition: Int? = null
    private var mAdapter: TimeLineDataSourceAdapter? = null
    private val mPref by lazy { act.pref }
    private var mIsStreamEnabled = true
    private var mIsEnableAutoScroll = true
    private var mIsStreamStart = false

    companion object {
        private val EXTRA_TIMELINE_INFO = "EXTRA_TIMELINE_INFO"
        private val DEFAULT_PAGE_SIZE = 30
        private val EXTRA_RECYCLER_TOP_CHILD_ADAPTER_POSITION = "RECYCLER_TOP_CHILD_ADAPTER_POSITION"
        private val TAG_OPERATION_TWEET = "TAG_OPERATE_TWEET"
        fun create(info: TimelineInfo): TimelineFragment = TimelineFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_TIMELINE_INFO, info)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            = createView(ctx)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let { restoreInstanceState(it) }
        Logger.v(javaClass.name, "onViewCreated() restore:${savedInstanceState != null}")
        loadPrefSetting()
        bindLaunch {
            val res = mModel.initialize(mTlInfo).await()
            val client = res.orError {
                toast(R.string.message_failed_acquire_info_necessary_for_browsing_timeline)
            } ?: return@bindLaunch
            initialize(client)
        }
    }

    private fun initialize(client: TwitterClient) {
        fun create() = TweetViewHolder(ctx, client, this, mModel, this::onTweetItemClick)
        val adapter = TimeLineDataSourceAdapter(this, mModel.dataSource, ::create)
        mAdapter = adapter
        adapter.pageSize = DEFAULT_PAGE_SIZE
        recycler.adapter = adapter
        adapter.setStoreLiveData(mModel.storeTweetsLiveData)
        adapter.error = {
            toast(R.string.message_failure_acquire_tweet)
        }
        if (adapter.restore()) {
            Logger.v(javaClass.name, "restore adapter")
            mSavedTopChildAdapterPosition?.let {
                recycler.scrollToPosition(it)
            }
            startStream()
        } else {
            Logger.v(javaClass.name, "simpleInit adapter")
            initializeAdapter()
        }
    }

    private fun initializeAdapter() {
        val adapter = mAdapter ?: throw IllegalStateException()
        swipeRefresh.isEnabled = false
        swipeRefresh.isRefreshing = true
        adapter.loadInitial {
            swipeRefresh.isRefreshing = false
            swipeRefresh.isEnabled = true
            startStream()
        }
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            adapter.loadAfter {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun startStream() {
        mIsStreamStart = mModel.startStream(this) {
            if (mIsStreamEnabled) {
                mAdapter!!.insertTop(it)
                if (mIsEnableAutoScroll) autoScroll()
            } else {
                mAdapter!!.store(it)
            }
        }
        setSwipeRefreshEnabled()
    }

    private fun setSwipeRefreshEnabled() {
        if (mIsStreamStart) {
            swipeRefresh.isEnabled = !mIsStreamEnabled
            Logger.v(logTag, "swipeRefresh isEnabled:${!mIsStreamEnabled}")
        } else {
            swipeRefresh.isEnabled = true
            Logger.v(logTag, "swipeRefresh isEnabled:true")
        }
    }

    override fun onResume() {
        super.onResume()
        loadPrefSetting()
        setSwipeRefreshEnabled()
    }

    private fun loadPrefSetting() {
        val prevStreamEnabled = mIsStreamEnabled
        mIsStreamEnabled = mPref.getBoolean(getString(R.string.pref_key_is_stream_enabled), true)
        mIsEnableAutoScroll = mPref.getBoolean(getString(R.string.pref_key_auto_scroll), true)
        if (mIsStreamStart && prevStreamEnabled != mIsStreamEnabled) {
            changeStreamSetting()
        }
        Logger.v(logTag, "isStreamEnabled $mIsStreamEnabled")
    }

    private fun changeStreamSetting() {
        if (mIsStreamEnabled) {
            mAdapter!!.marge()
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        getChildAdapterPosition()?.let {
            outState.putInt(EXTRA_RECYCLER_TOP_CHILD_ADAPTER_POSITION, it)
        }
        super.onSaveInstanceState(outState)
    }

    private fun getChildAdapterPosition(): Int? {
        val adapter = mAdapter
        val topChild = recycler.getChildAt(0)
        return if (adapter != null && topChild != null) {
            val childAt = recycler.getChildAt(0)
            recycler.getChildAdapterPosition(childAt)
        } else null
    }

    private fun onTweetItemClick(tweet: Tweet) {
        OperationTweetDialogFragment.create(tweet).show(childFragmentManager, TAG_OPERATION_TWEET)
    }


    private fun autoScroll() {
        getChildAdapterPosition()?.let { if (it == 1) scrollToTop() }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        mSavedTopChildAdapterPosition = savedInstanceState.getInt(EXTRA_RECYCLER_TOP_CHILD_ADAPTER_POSITION)
    }

    fun scrollToTop() = recycler.scrollToPosition(0)

    class OperationTweetDialogFragment : BottomSheetDialogFragment() {
        private val mTweet by lazy { arg.getSerializable(EXTRA_TWEET) as Tweet }
        private val tlFragment by lazy { parentFragment as TimelineFragment }
        private val mClientId by lazy { tlFragment.mTlInfo.accountId }
        private val mModel by lazy { tlFragment.mModel }


        companion object {
            private val EXTRA_TWEET = "EXTRA_TWEET"

            fun create(tweet: Tweet) = OperationTweetDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_TWEET, tweet)
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = super.onCreateDialog(savedInstanceState)
            val menus = ArrayList<View>()
            menus.add(createFavoriteMenu())
            menus.add(createRetweetMenu())
            menus.add(createReplyMenu())
            createReplyAllMenu()?.let { menus.add(it) }
            createUserInfoOpenMenu().forEach { menus.add(it) }
            createMediaOpenMenu()?.let { menus.add(it) }
            createHashtagMenus().forEach { menus.add(it) }
            createUrlOpenMenus().forEach { menus.add(it) }
            menus.add(createLinkOpenMenu())

            val component = createBottomSheetUIComponent(ctx, mTweet.text, *menus.toTypedArray())
            dialog.setContentView(component.componentRoot)
            return dialog
        }

        private fun createFavoriteMenu(): View {
            val nonFavIconRes = R.drawable.ic_star_border_white_24dp
            val favIconRes = R.drawable.ic_star_white_24dp
            val component = createMenuItemUIComponent(ctx, nonFavIconRes, R.string.create_favorite)
            return component.componentRoot
        }

        private fun createRetweetMenu(): View {
            val nonRtIconRes = R.drawable.ic_autorenew_white_24dp
            val rtIconRes = R.drawable.ic_check_white_24dp
            val component = createMenuItemUIComponent(ctx, nonRtIconRes, R.string.create_retweet)
            return component.componentRoot
        }

        private fun createUrlOpenMenus(): List<View> {
            val openIconRes = R.drawable.ic_open_in_browser_white_24dp
            fun create(entity: UrlEntity): View {
                val url = entity.expandUrl
                val view = createMenuItemUIComponent(ctx, openIconRes, url).componentRoot
                view.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    dismiss()
                }
                return view
            }
            return mTweet.urlEntities.map(::create)
        }

        private fun createHashtagMenus(): List<View> {
            val editIconRes = R.drawable.ic_mode_edit_white_24dp
            fun create(hashtag: String): View {
                val view = createMenuItemUIComponent(ctx, editIconRes, "#$hashtag").componentRoot
                view.setOnClickListener {
                    toast("Unimplemented")//todo Unimplemented
                    dismiss()
                }
                return view
            }
            return mTweet.hashtags.map(::create)
        }

        private fun createMediaOpenMenu(): View? {
            fun create(entities: List<MediaEntity>): View {
                val imageIconRes = R.drawable.ic_image_white_24dp
                val view = createMenuItemUIComponent(ctx, imageIconRes, R.string.open_media).componentRoot
                view.setOnClickListener {
                    toast("Unimplemented")//todo Unimplemented
                    dismiss()
                }
                return view
            }
            return mTweet.mediaEntities.takeIf { it.isNotEmpty() }?.let {
                create(it)
            }
        }

        private fun createUserInfoOpenMenu(): List<View> {
            val userIconRes = R.drawable.ic_person_white_24dp
            fun create(user: Pair<String, Long>): View {
                val view = createMenuItemUIComponent(ctx, userIconRes, user.first).componentRoot
                view.setOnClickListener {
                    toast("Unimplemented")//todo Unimplemented
                    dismiss()
                }
                return view
            }

            val users = ArrayList<Pair<String, Long>>()
            val user = mTweet.user
            users.add(user.screenName to user.id)
            mTweet.retweetedTweet?.user?.let { users.add(it.screenName to it.id) }
            return (users + mTweet.userMentionEntities.map { it.screenName to it.id })
                    .distinctBy { it.second }.map(::create)
        }

        private fun createLinkOpenMenu(): View {
            val openIconRes = R.drawable.ic_open_in_browser_white_24dp
            val view = createMenuItemUIComponent(ctx, openIconRes, R.string.open_tweet_link).componentRoot
            view.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mTweet.link)))
                dismiss()
            }
            return view
        }

        private fun createReplyMenu(): View {
            val replyIcon = R.drawable.ic_reply_white_24dp
            val t = mTweet.retweetedTweet ?: mTweet
            val view = createMenuItemUIComponent(ctx, replyIcon, R.string.reply).componentRoot
            view.setOnClickListener {
                toast("Unimplemented")//todo Unimplemented
                dismiss()
            }
            return view
        }

        private fun createReplyAllMenu(): View? {
            val t = mTweet.retweetedTweet ?: mTweet
            fun create(): View {
                val replyAllIcon = R.drawable.ic_reply_all_white_24px
                val view = createMenuItemUIComponent(ctx, replyAllIcon, R.string.reply_all).componentRoot
                view.setOnClickListener {
                    toast("Unimplemented")//todo Unimplemented
                    dismiss()
                }
                return view
            }
            return t.userMentionEntities.takeIf { it.isNotEmpty() }?.let {
                create()
            }
        }
    }
}