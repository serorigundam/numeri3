package net.ketc.numeri.presentation.view.component

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.view.View
import net.ketc.numeri.R
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.cache.isFavorite
import net.ketc.numeri.domain.model.cache.isRetweeted
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.presenter.component.TweetOperatorDialogPresenter
import net.ketc.numeri.presentation.view.component.ui.*
import net.ketc.numeri.util.rx.AutoDisposable

class TweetOperatorDialogFactory(private val ctx: Context,
                                 private val tweet: Tweet,
                                 autoDisposable: AutoDisposable,
                                 error: (Throwable) -> Unit) {
    val presenter = TweetOperatorDialogPresenter(ctx, autoDisposable, tweet, error)
    private val dialog = BottomSheetDialog(ctx).apply {
        setContentView(TweetOperationDialogUI(ctx).createView())
    }

    fun create(client: TwitterClient): BottomSheetDialog {
        dialog.tweetText.text = tweet.text
        dialog.addTweetMenu(createFavoriteMenu(client))
        dialog.addTweetMenu(createRetweetMenu(client))
        return dialog
    }

    private fun createFavoriteMenu(client: TwitterClient): View {
        val textId: Int
        val iconId: Int
        if (client.isFavorite(tweet)) {
            textId = R.string.destroy_favorite
            iconId = R.drawable.ic_star_white_24dp
        } else {
            textId = R.string.create_favorite
            iconId = R.drawable.ic_star_border_white_24dp
        }

        return createIconMenu(ctx, iconId, textId) { v ->
            v.isClickable = false
            presenter.changeFavorite(client, {
                v.isClickable = true
            }, {
                if (it) {
                    v.menuText.setText(R.string.destroy_favorite)
                    v.iconImage.setImageDrawable(ctx.getDrawable(R.drawable.ic_star_white_24dp))
                } else {
                    v.menuText.setText(R.string.create_favorite)
                    v.iconImage.setImageDrawable(ctx.getDrawable(R.drawable.ic_star_border_white_24dp))
                }
                v.isClickable = true
            })
        }
    }

    private fun createRetweetMenu(client: TwitterClient): View {
        val textId: Int
        val iconId: Int
        if (client.isRetweeted(tweet)) {
            textId = R.string.destroy_retweet
            iconId = R.drawable.ic_check_white_24dp
        } else {
            textId = R.string.create_retweet
            iconId = R.drawable.ic_autorenew_white_24dp
        }
        return createIconMenu(ctx, iconId, textId) { v ->
            v.isClickable = false
            presenter.changeRetweeted(client, {
                v.isClickable = true
            }, {
                if (it) {
                    v.menuText.setText(R.string.destroy_retweet)
                    v.iconImage.setImageDrawable(ctx.getDrawable(R.drawable.ic_check_white_24dp))
                } else {
                    v.iconImage.setImageDrawable(ctx.getDrawable(R.drawable.ic_autorenew_white_24dp))
                    v.menuText.setText(R.string.create_retweet)
                }
                v.isClickable = true
            })
        }
    }
}
