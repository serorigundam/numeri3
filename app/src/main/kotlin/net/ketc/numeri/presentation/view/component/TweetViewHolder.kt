package net.ketc.numeri.presentation.view.component

import android.graphics.Color
import android.view.View
import io.reactivex.disposables.Disposable
import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.isMention
import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.presentation.view.activity.MediaActivity
import net.ketc.numeri.presentation.view.activity.UserInfoActivity
import net.ketc.numeri.presentation.view.component.adapter.ReadableMoreViewHolder
import net.ketc.numeri.presentation.view.component.ui.ITweetViewUI
import net.ketc.numeri.util.android.download
import net.ketc.numeri.util.android.getResourceId
import net.ketc.numeri.util.rx.AutoDisposable
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.image

class TweetViewHolder(ui: ITweetViewUI,
                      override val autoDisposable: AutoDisposable,
                      private val client: TwitterClient,
                      onClick: (Tweet) -> Unit) :
        ReadableMoreViewHolder<Tweet>(ui, onClick),
        AutoDisposable by autoDisposable,
        ITweetViewUI by ui {
    private var previousLoadImageDisposable: Disposable? = null

    init {
        itemView.backgroundResource = ctx.getResourceId(android.R.attr.selectableItemBackground)
        itemView.isClickable = true
    }

    override fun bind(value: Tweet) {
        val displayTweet = value.retweetedTweet ?: value
        setColor(value)
        setSubInfo(value)
        setDisplayTweet(displayTweet)
        setThumbs(displayTweet)
        itemView.setOnClickListener { onClick(value) }
    }


    private fun setSubInfo(tweet: Tweet) {
        if (tweet.retweetedTweet != null) {
            subInfoText.visibility = View.VISIBLE
            subInfoText.text = "${tweet.user.screenName}さんからのRT"
            subInfoIcon.image = ctx.getDrawable(net.ketc.numeri.R.drawable.ic_autorenew_white_24dp)
        } else {
            subInfoText.visibility = View.GONE
            subInfoText.text = ""
            subInfoIcon.image = null
        }
    }

    private fun setDisplayTweet(displayTweet: Tweet) {
        createdAtText.text = displayTweet.createdAt
        screenNameText.text = displayTweet.user.screenName
        userNameText.text = displayTweet.user.name
        previousLoadImageDisposable?.takeUnless { it.isDisposed }?.dispose()
        previousLoadImageDisposable = iconImage.download(displayTweet.user.iconUrl, autoDisposable)
        iconImage.setOnClickListener { UserInfoActivity.start(ctx, client.id, displayTweet.user.id) }
        text.text = displayTweet.text
        sourceText.text = displayTweet.source
    }

    private fun setThumbs(displayTweet: Tweet) {
        val thumbs = displayTweet.mediaEntities.map { it.url + ":thumb" }
        (0..3).map { thumbs.getOrElse(it) { "" } }.forEachIndexed { i, url ->
            if (url.isNotEmpty()) {
                thumbnails[i].visibility = View.VISIBLE
                thumbnails[i].download(url, autoDisposable)
                thumbnails[i].setOnClickListener { MediaActivity.start(ctx, displayTweet.mediaEntities, i) }
            } else {
                thumbnails[i].setImageDrawable(null)
                thumbnails[i].visibility = View.GONE
            }
        }
    }

    private fun setColor(tweet: Tweet) {
        if (tweet.isMention(client)) {
            overlayRelative.backgroundColor = Color.parseColor("#60f46e42")
        } else if (tweet.retweetedTweet != null) {
            overlayRelative.backgroundColor = Color.parseColor("#6042dff4")
        } else {
            overlayRelative.backgroundColor = Color.parseColor("#00000000")
        }
    }
}