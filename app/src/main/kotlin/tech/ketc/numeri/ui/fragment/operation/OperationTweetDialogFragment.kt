package tech.ketc.numeri.ui.fragment.operation

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View
import org.jetbrains.anko.image
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.*
import tech.ketc.numeri.ui.activity.conversation.ConversationActivity
import tech.ketc.numeri.ui.activity.media.MediaActivity
import tech.ketc.numeri.ui.activity.tweet.TweetActivity
import tech.ketc.numeri.ui.activity.user.UserInfoActivity
import tech.ketc.numeri.ui.components.createBottomSheetUIComponent
import tech.ketc.numeri.ui.components.createMenuItemUIComponent
import tech.ketc.numeri.ui.model.delegate.HasTweetOperator
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.act
import tech.ketc.numeri.util.android.arg
import tech.ketc.numeri.util.android.ui.gesture.SimpleDoubleClickHelper
import tech.ketc.numeri.util.arch.owner.bindLaunch
import tech.ketc.numeri.util.arch.response.orError
import tech.ketc.numeri.util.logTag
import tech.ketc.numeri.util.twitter4j.showTwitterError
import java.util.ArrayList

class OperationTweetDialogFragment : BottomSheetDialogFragment() {
    private val mTweet by lazy { arg.getSerializable(EXTRA_TWEET) as Tweet }
    private val mClient by lazy { arg.getSerializable(EXTRA_CLIENT) as TwitterClient }
    private val mOperator by lazy {
        (act as? HasTweetOperator)?.operator
                ?: (parentFragment as? HasTweetOperator)?.operator
                ?: throw IllegalStateException()
    }

    private val stateHandleTweet: Tweet
        get() = mTweet.retweetedTweet ?: mTweet
    private val state: TweetState
        get() = mOperator.getState(mClient, stateHandleTweet)


    companion object {
        private val EXTRA_TWEET = "EXTRA_TWEET"
        private val EXTRA_CLIENT = "EXTRA_CLIENT"

        fun create(client: TwitterClient, tweet: Tweet) = OperationTweetDialogFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_CLIENT, client)
                putSerializable(EXTRA_TWEET, tweet)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val menus = ArrayList<View>()
        val handleTweetOwnerIsProtected = stateHandleTweet.user.isProtected
        menus.add(createFavoriteMenu())
        if (!handleTweetOwnerIsProtected) menus.add(createRetweetMenu())
        menus.add(createReplyMenu())
        createReplyAllMenu()?.let { menus.add(it) }
        createConversationShowMenu()?.let { menus.add(it) }
        createUserInfoOpenMenu().forEach { menus.add(it) }
        createMediaOpenMenu()?.let { menus.add(it) }
        createAllHashtagTweetMenu()?.let { menus.add(it) }
        createHashtagTweetMenus().forEach { menus.add(it) }
        createUrlOpenMenus().forEach { menus.add(it) }
        menus.add(createLinkOpenMenu())
        createDeleteTweetMenu()?.let { menus.add(it) }

        val message = "@${stateHandleTweet.user.screenName} : ${stateHandleTweet.text}"
        val component = createBottomSheetUIComponent(ctx, message, *menus.toTypedArray())
        dialog.setContentView(component.componentRoot)
        return dialog
    }

    private fun createFavoriteMenu(): View {
        var isFav = state.isFavorited
        val nonFavIconRes = R.drawable.ic_star_border_white_24dp
        val favIconRes = R.drawable.ic_star_white_24dp
        val favMessage = R.string.destroy_favorite
        val nonFavMessage = R.string.create_favorite
        fun icon(isFav: Boolean) = if (isFav) favIconRes else nonFavIconRes
        fun message(isFav: Boolean) = if (isFav) favMessage else nonFavMessage

        val component = createMenuItemUIComponent(ctx, icon(isFav), message(isFav))
        fun set(isFav: Boolean) {
            component.imageView.image = ctx.getDrawable(icon(isFav))
            component.textView.text = ctx.getString(message(isFav))
            component.componentRoot.isClickable = true
        }

        val view = component.componentRoot
        view.setOnClickListener {
            view.isClickable = false
            bindLaunch {
                isFav = if (isFav) {
                    mOperator.unfavorite(mClient, stateHandleTweet).await().orError {
                        showTwitterError(it)
                    } ?: return@bindLaunch set(isFav)
                    false
                } else {
                    mOperator.favorite(mClient, stateHandleTweet).await().orError {
                        showTwitterError(it)
                    } ?: return@bindLaunch set(isFav)
                    true
                }
                set(isFav)
            }
        }
        return view
    }

    private fun createRetweetMenu(): View {
        var isRt = state.isRetweeted
        val nonRtIconRes = R.drawable.ic_autorenew_white_24dp
        val rtIconRes = R.drawable.ic_check_white_24dp
        val rtMessage = R.string.destroy_retweet
        val nonRtMessage = R.string.create_retweet
        fun icon(isFav: Boolean) = if (isFav) rtIconRes else nonRtIconRes
        fun message(isFav: Boolean) = if (isFav) rtMessage else nonRtMessage

        val component = createMenuItemUIComponent(ctx, icon(isRt), message(isRt))
        fun set(isFav: Boolean) {
            component.imageView.image = ctx.getDrawable(icon(isFav))
            component.textView.text = ctx.getString(message(isFav))
            component.componentRoot.isClickable = true
        }

        val view = component.componentRoot
        view.setOnClickListener {
            view.isClickable = false
            bindLaunch {
                isRt = if (isRt) {
                    mOperator.unretweet(mClient, stateHandleTweet).await().orError {
                        Logger.printStackTrace(this@OperationTweetDialogFragment.logTag, it)
                        (it as? IllegalStateException)?.let {
                            toast(R.string.failed_find_retweeted_id)
                        }
                        showTwitterError(it)
                    } ?: return@bindLaunch set(isRt)
                    false
                } else {
                    mOperator.retweet(mClient, stateHandleTweet).await().orError {
                        Logger.printStackTrace(this@OperationTweetDialogFragment.logTag, it)
                        showTwitterError(it)
                    } ?: return@bindLaunch set(isRt)
                    true
                }
                set(isRt)
            }
        }
        return view
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
        return stateHandleTweet.urlEntities.map(::create)
    }

    private fun createHashtagTweetMenus(): List<View> {
        val editIconRes = R.drawable.ic_mode_edit_white_24dp
        fun create(hashtag: String): View {
            val text = "#$hashtag"
            val view = createMenuItemUIComponent(ctx, editIconRes, text).componentRoot
            view.setOnClickListener {
                TweetActivity.start(ctx, text = "$text ", client = mClient)
                dismiss()
            }
            return view
        }
        return stateHandleTweet.hashtags.map(::create)
    }

    private fun createAllHashtagTweetMenu(): View? {
        val editIconRes = R.drawable.ic_mode_edit_white_24dp
        return stateHandleTweet.hashtags.takeIf { it.size >= 2 }?.let { hashtags ->
            createMenuItemUIComponent(ctx, editIconRes, R.string.all_hashtag_tweet)
                    .componentRoot.also {
                it.setOnClickListener {
                    val text = hashtags.joinToString(" ") { "#$it" }
                    TweetActivity.start(ctx, text = "$text ")
                }
            }
        }
    }

    private fun createMediaOpenMenu(): View? {
        fun create(entities: List<MediaEntity>): View {
            val imageIconRes = R.drawable.ic_image_white_24dp
            val view = createMenuItemUIComponent(ctx, imageIconRes, R.string.open_media).componentRoot
            view.setOnClickListener {
                val entity = entities.singleOrNull()
                if (entity != null && (entity.type == MediaType.VIDEO
                        || entity.type == MediaType.ANIMATED_GIF)) {
                    val variant = entity.variants.maxBy { it.bitrate }!!
                    ctx.startActivity(variant.toIntent())
                } else {
                    MediaActivity.start(ctx, entities, stateHandleTweet.user.screenName)
                }
                dismiss()
            }
            return view
        }
        return stateHandleTweet.mediaEntities.takeIf { it.isNotEmpty() }?.let {
            create(it)
        }
    }

    private fun createUserInfoOpenMenu(): List<View> {
        val userIconRes = R.drawable.ic_person_white_24dp
        fun create(user: Pair<String, Long>): View {
            val view = createMenuItemUIComponent(ctx, userIconRes, user.first).componentRoot
            view.setOnClickListener {
                toast("Unimplemented")//todo Unimplemented
                UserInfoActivity.start(ctx, mClient, user.second)
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
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(stateHandleTweet.link)))
            dismiss()
        }
        return view
    }

    private fun createReplyMenu(): View {
        val replyIcon = R.drawable.ic_reply_white_24dp
        val view = createMenuItemUIComponent(ctx, replyIcon, R.string.reply).componentRoot
        view.setOnClickListener {
            TweetActivity.start(ctx, client = mClient, tweet = mTweet)
            dismiss()
        }
        return view
    }

    private fun createReplyAllMenu(): View? {
        fun create(): View {
            val replyAllIcon = R.drawable.ic_reply_all_white_24px
            val view = createMenuItemUIComponent(ctx, replyAllIcon, R.string.reply_all).componentRoot
            view.setOnClickListener {
                TweetActivity.start(ctx, client = mClient, tweet = mTweet, replyAll = true)
                dismiss()
            }
            return view
        }
        return stateHandleTweet.userMentionEntities.takeIf { it.isNotEmpty() }?.let {
            create()
        }
    }

    private fun createDeleteTweetMenu(): View? {
        return stateHandleTweet.takeIf { it.user.id == mClient.id }?.let { tweet ->
            val deleteIcon = R.drawable.ic_delete_forever_white_24px
            val component = createMenuItemUIComponent(ctx, deleteIcon, R.string.delete_tweet)
            component.componentRoot.apply {
                SimpleDoubleClickHelper {
                    bindLaunch {
                        mOperator.delete(mClient, tweet).await().orError {
                            toast(R.string.failed_tweet_delete)
                        } ?: return@bindLaunch
                        toast(R.string.tweet_deleted)
                    }
                    dismiss()
                }.attachTo(this)
            }
        }
    }

    private fun createConversationShowMenu(): View? {
        return stateHandleTweet.inReplyToStatusId.takeIf { it != -1L }?.let {
            val conversationIcon = R.drawable.ic_chat_bubble_outline_white_24dp
            val component = createMenuItemUIComponent(ctx, conversationIcon, R.string.follow_conversation)
            component.componentRoot.apply {
                setOnClickListener {
                    ConversationActivity.start(ctx, mClient, mTweet)
                    dismiss()
                }
            }
        }
    }
}