package net.ketc.numeri.presentation.view.component

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.view.View
import net.ketc.numeri.R
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.UrlEntity
import net.ketc.numeri.domain.model.cache.*
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.component.TweetOperateDialogPresenter
import net.ketc.numeri.presentation.view.activity.ConversationActivity
import net.ketc.numeri.presentation.view.activity.MediaActivity
import net.ketc.numeri.presentation.view.activity.TweetActivity
import net.ketc.numeri.presentation.view.activity.UserInfoActivity
import net.ketc.numeri.presentation.view.component.ui.menu.*
import net.ketc.numeri.presentation.view.component.ui.dialog.BottomSheetDialogUI
import net.ketc.numeri.presentation.view.component.ui.dialog.addMenu
import net.ketc.numeri.presentation.view.component.ui.dialog.messageText
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.MySchedulers
import net.ketc.numeri.util.toImmutableList
import org.jetbrains.anko.image
import java.util.ArrayList

class TweetOperatorDialogFactory(private val ctx: Context,
                                 private val tweet: Tweet,
                                 private val autoDisposable: AutoDisposable,
                                 private val error: (Throwable) -> Unit) {
    private val dialog = BottomSheetDialog(ctx).apply {
        setContentView(BottomSheetDialogUI(ctx).createView())
    }


    fun create(client: TwitterClient): BottomSheetDialog {
        val menuItems = TweetMenuItems(ctx, tweet, client, dialog, autoDisposable, error)
        dialog.messageText.text = tweet.text
        dialog.addMenu(menuItems.favoriteMenuItem)
        if (!(tweet.retweetedTweet ?: tweet).user.isProtected) {
            dialog.addMenu(menuItems.retweetMenuItem)
        }
        dialog.addMenu(menuItems.replyMenu)
        menuItems.openUserMenuItems.forEach {
            dialog.addMenu(it)
        }
        menuItems.openUrlMenuItems.forEach {
            dialog.addMenu(it)
        }
        menuItems.openDisplayConversationItem?.let {
            dialog.addMenu(it)
        }
        menuItems.openMediaItem?.let {
            dialog.addMenu(it)
        }
        dialog.addMenu(menuItems.openTweetLink)
        return dialog
    }
}


interface TweetMenuItemsInterface {
    var isFavorite: Boolean
    var isFavoriteMenuClickable: Boolean
    var isRetweeted: Boolean
    var isRetweetMenuClickable: Boolean
    val tweet: Tweet
    val client: TwitterClient
}

class TweetMenuItems(private val ctx: Context,
                     override val tweet: Tweet,
                     override val client: TwitterClient,
                     private val dialog: BottomSheetDialog,
                     autoDisposable: AutoDisposable,
                     error: (Throwable) -> Unit) : TweetMenuItemsInterface {
    private val presenter = TweetOperateDialogPresenter(ctx, autoDisposable, this, error)
    private val displayTweet = tweet.retweetedTweet ?: tweet
    override var isFavorite: Boolean
        get() = client.checkFavorite(displayTweet)
        set(value) {
            val ctx = favoriteMenuItem.context
            if (value) {
                favoriteMenuItem.menuText.setText(R.string.destroy_favorite)
                favoriteMenuItem.iconImage.setImageDrawable(ctx.getDrawable(R.drawable.ic_star_white_24dp))
            } else {
                favoriteMenuItem.menuText.setText(R.string.create_favorite)
                favoriteMenuItem.iconImage.setImageDrawable(ctx.getDrawable(R.drawable.ic_star_border_white_24dp))
            }
        }

    override var isFavoriteMenuClickable: Boolean
        get() = favoriteMenuItem.isClickable
        set(value) {
            favoriteMenuItem.isClickable = value
        }
    override var isRetweeted: Boolean
        get() = client.checkRetweeted(displayTweet)
        set(value) {
            val ctx = retweetMenuItem.context
            if (value) {
                retweetMenuItem.menuText.setText(R.string.destroy_retweet)
                retweetMenuItem.iconImage.setImageDrawable(ctx.getDrawable(R.drawable.ic_check_white_24dp))
            } else {
                retweetMenuItem.menuText.setText(R.string.create_retweet)
                retweetMenuItem.iconImage.setImageDrawable(ctx.getDrawable(R.drawable.ic_autorenew_white_24dp))
            }
        }

    override var isRetweetMenuClickable: Boolean
        get() = retweetMenuItem.isClickable
        set(value) {
            retweetMenuItem.isClickable = value
        }


    val favoriteMenuItem = createFavoriteMenu()
    val retweetMenuItem = createRetweetMenu()
    val openUserMenuItems = createOpenUserMenus()
    val openUrlMenuItems = createOpenUrlMenus()
    val openDisplayConversationItem = displayTweet.inReplyToStatusId
            .takeUnless { it == -1L }?.let { createDisplayConversationMenu() }
    val openMediaItem = displayTweet.mediaEntities
            .takeUnless { it.isEmpty() }?.let { createOpenMediaMenu() }
    val openTweetLink = createOpenTweetLinkMenu()
    val replyMenu = createReplyMenu()


    private fun createFavoriteMenu(): View {
        val menu = createIconMenu(ctx, R.drawable.ic_sync_white_24dp, R.string.sync_favorite) { _ -> presenter.changeFavorite() }
        menu.isClickable = false
        presenter.singleTask(MySchedulers.twitter) {
            client.checkFavoriteOrElse(displayTweet) {
                val result = client.getTweet(displayTweet.id)
                client.checkFavorite(result)
            }
        } error {
            menu.iconImage.image = ctx.getDrawable(R.drawable.ic_sync_problem_white_24dp)
            menu.menuText.text = ctx.getString(R.string.sync_failure)
        } success {
            val textId: Int
            val iconId: Int
            if (it) {
                textId = R.string.destroy_favorite
                iconId = R.drawable.ic_star_white_24dp
            } else {
                textId = R.string.create_favorite
                iconId = R.drawable.ic_star_border_white_24dp
            }
            menu.iconImage.image = ctx.getDrawable(iconId)
            menu.menuText.text = ctx.getString(textId)
            menu.isClickable = true
        }
        return menu
    }

    private fun createRetweetMenu(): View {
        val menu = createIconMenu(ctx, R.drawable.ic_sync_white_24dp, R.string.sync_favorite) { _ -> presenter.changeRetweeted() }
        menu.isClickable = false
        presenter.singleTask(MySchedulers.twitter) {
            client.checkRetwwtedOrElse(displayTweet) {
                val result = client.getTweet(displayTweet.id)
                client.checkRetweeted(result)
            }
        } error {
            menu.iconImage.image = ctx.getDrawable(R.drawable.ic_sync_problem_white_24dp)
            menu.menuText.text = ctx.getString(R.string.sync_failure)
        } success {
            val textId: Int
            val iconId: Int
            if (it) {
                textId = R.string.destroy_retweet
                iconId = R.drawable.ic_check_white_24dp
            } else {
                textId = R.string.create_retweet
                iconId = R.drawable.ic_autorenew_white_24dp
            }
            menu.iconImage.image = ctx.getDrawable(iconId)
            menu.menuText.text = ctx.getString(textId)
            menu.isClickable = true
        }
        return menu
    }

    private fun createOpenUrlMenus(): List<View> {
        fun UrlEntity.createMenu(): View {
            return createIconMenu(ctx, R.drawable.ic_open_in_browser_white_24dp, expandUrl) {
                presenter.openUri(expandUrl)
                dialog.dismiss()
            }
        }
        return displayTweet.urlEntities.map(UrlEntity::createMenu).toImmutableList()
    }

    private fun createOpenUserMenus(): List<View> {
        fun Pair<Long, String>.createMenu(): View {
            return createIconMenu(ctx, R.drawable.ic_account_circle_white_24dp, "@$second") {
                UserInfoActivity.start(ctx, client.id, first)
                dialog.dismiss()
            }
        }

        val list1 = ArrayList<Pair<Long, String>>().apply {
            tweet.retweetedTweet?.user?.let {
                add(it.id to it.screenName)
            }
            add(tweet.user.id to tweet.user.screenName)
        }
        val list2 = displayTweet.userMentionEntities.map { it.id to it.screenName }
        return (list1 + list2).distinctBy { it.first }.map(Pair<Long, String>::createMenu)
    }

    private fun createDisplayConversationMenu(): View {
        return createIconMenu(ctx, R.drawable.ic_chat_bubble_outline_white_24dp, R.string.follow_conversation) {
            ConversationActivity.start(ctx, displayTweet.id, client)
            dialog.dismiss()
        }
    }

    private fun createOpenMediaMenu(): View {
        return createIconMenu(ctx, R.drawable.ic_image_white_24dp, R.string.open_media) {
            MediaActivity.start(ctx, displayTweet.mediaEntities)
            dialog.dismiss()
        }
    }

    private fun createOpenTweetLinkMenu(): View {
        return createIconMenu(ctx, R.drawable.ic_open_in_browser_white_24dp, R.string.open_tweet_link) {
            presenter.openUri("https://twitter.com/${tweet.user.screenName}/status/${tweet.id}")
            dialog.dismiss()
        }
    }

    private fun createReplyMenu(): View {
        return createIconMenu(ctx, R.drawable.ic_reply_white_24dp, R.string.reply) {
            TweetActivity.start(ctx, client.id, displayTweet)
            dialog.dismiss()
        }
    }
}