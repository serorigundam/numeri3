package net.ketc.numeri.presentation.view.component

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.view.View
import net.ketc.numeri.R
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.UrlEntity
import net.ketc.numeri.domain.model.cache.isFavorite
import net.ketc.numeri.domain.model.cache.isRetweeted
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.component.TweetOperatorDialogPresenter
import net.ketc.numeri.presentation.view.component.ui.*
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.toImmutableList

class TweetOperatorDialogFactory(private val ctx: Context,
                                 private val tweet: Tweet,
                                 private val autoDisposable: AutoDisposable,
                                 private val error: (Throwable) -> Unit) {
    private val dialog = BottomSheetDialog(ctx).apply {
        setContentView(TweetOperationDialogUI(ctx).createView())
    }


    fun create(client: TwitterClient): BottomSheetDialog {
        val menuItems = TweetMenuItems(ctx, tweet, client, autoDisposable, error)
        dialog.tweetText.text = tweet.text
        dialog.addTweetMenu(menuItems.favoriteMenuItem)
        dialog.addTweetMenu(menuItems.retweetMenuItem)
        menuItems.openUrlMenuItems.forEach {
            dialog.addTweetMenu(it)
        }
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
                     autoDisposable: AutoDisposable,
                     error: (Throwable) -> Unit) : TweetMenuItemsInterface {
    private val presenter = TweetOperatorDialogPresenter(ctx, autoDisposable, this, error)
    override var isFavorite: Boolean
        get() = client.isFavorite(tweet)
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
        get() = client.isRetweeted(tweet)
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


    val favoriteMenuItem: View = createFavoriteMenu()
    val retweetMenuItem: View = createRetweetMenu()
    val openUrlMenuItems: List<View> = createOpenUrlMenus()

    private fun createFavoriteMenu(): View {
        val textId: Int
        val iconId: Int
        if (client.isFavorite(tweet)) {
            textId = R.string.destroy_favorite
            iconId = R.drawable.ic_star_white_24dp
        } else {
            textId = R.string.create_favorite
            iconId = R.drawable.ic_star_border_white_24dp
        }
        return createIconMenu(ctx, iconId, textId) { v -> presenter.changeFavorite() }
    }

    private fun createRetweetMenu(): View {
        val textId: Int
        val iconId: Int
        if (client.isRetweeted(tweet)) {
            textId = R.string.destroy_retweet
            iconId = R.drawable.ic_check_white_24dp
        } else {
            textId = R.string.create_retweet
            iconId = R.drawable.ic_autorenew_white_24dp
        }
        return createIconMenu(ctx, iconId, textId) { v -> presenter.changeRetweeted() }
    }

    private fun createOpenUrlMenus(): List<View> {
        fun UrlEntity.createMenu(): View {
            return createIconMenu(ctx, R.drawable.ic_open_in_browser_white_24dp, expandUrl) {
                presenter.openUri(expandUrl)
            }
        }
        return tweet.urlEntities.map(UrlEntity::createMenu).toImmutableList()
    }

}