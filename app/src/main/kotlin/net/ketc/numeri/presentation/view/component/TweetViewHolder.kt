package net.ketc.numeri.presentation.view.component

import android.content.Context
import android.graphics.Color
import android.view.View
import kotlinx.coroutines.experimental.channels.buildChannel
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.isMention
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.view.activity.MediaActivity
import net.ketc.numeri.presentation.view.component.adapter.TwitterViewHolder
import net.ketc.numeri.presentation.view.component.ui.menu.iconImage
import net.ketc.numeri.presentation.view.component.ui.tweet.*
import net.ketc.numeri.util.android.download
import net.ketc.numeri.util.android.getResourceId
import net.ketc.numeri.util.rx.AutoDisposable
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.image

class TweetViewHolder(ctx: Context,
                      override val autoDisposable: AutoDisposable,
                      private val client: TwitterClient,
                      onClick: (Tweet) -> Unit) :
        TwitterViewHolder<Tweet>(TweetViewUI(ctx).createView(), onClick),
        AutoDisposable by autoDisposable {

    init {
        itemView.backgroundResource = ctx.getResourceId(android.R.attr.selectableItemBackground)
        itemView.isClickable = true
    }

    override fun bind(cacheable: Tweet) {
        itemView.bind(cacheable)
        itemView.setOnClickListener { onClick(cacheable) }
    }

    private fun View.bind(tweet: Tweet) {
        val displayTweet = tweet.retweetedTweet ?: tweet
        setColor(tweet)
        setSubInfo(tweet)
        setDisplayTweet(displayTweet)
        setThumbs(displayTweet)
    }

    private fun View.setSubInfo(tweet: Tweet) {
        if (tweet.retweetedTweet != null) {
            subInfoText.visibility = View.VISIBLE
            subInfoText.text = "${tweet.user.screenName}さんからのRT"
            subInfoIcon.image = context.getDrawable(net.ketc.numeri.R.drawable.ic_autorenew_white_24dp)
        } else {
            subInfoText.visibility = View.GONE
            subInfoText.text = ""
            subInfoIcon.image = null
        }
    }

    private fun View.setDisplayTweet(displayTweet: Tweet) {
        createdAtText.text = displayTweet.createdAt
        screenNameText.text = displayTweet.user.screenName
        userNameText.text = displayTweet.user.name
        iconImage.download(displayTweet.user.iconUrl, autoDisposable)
        text.text = displayTweet.text
        sourceText.text = displayTweet.source
    }

    private fun View.setThumbs(displayTweet: Tweet) {
        val thumbs = displayTweet.mediaEntities.map { it.url + ":thumb" }
        (0..3).map { thumbs.getOrElse(it) { "" } }.forEachIndexed { i, url ->
            if (url.isNotEmpty()) {
                thumbnails[i].visibility = View.VISIBLE
                thumbnails[i].download(url, autoDisposable)
                thumbnails[i].setOnClickListener { MediaActivity.start(context, displayTweet.mediaEntities, i) }
            } else {
                thumbnails[i].setImageDrawable(null)
                thumbnails[i].visibility = View.GONE
            }
        }
    }

    private fun View.setColor(tweet: Tweet) {
        if (tweet.isMention(client)) {
            overlayRelative.backgroundColor = Color.parseColor("#60f46e42")
        } else if (tweet.retweetedTweet != null) {
            overlayRelative.backgroundColor = Color.parseColor("#6042dff4")
        } else {
            overlayRelative.backgroundColor = Color.parseColor("#00000000")
        }
    }
}