package tech.ketc.numeri.ui.view.recycler.timeline

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import kotlinx.coroutines.experimental.Job
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.image
import tech.ketc.numeri.R
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.isMention
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.ui.components.ITweetUIComponent
import tech.ketc.numeri.ui.components.TweetUIComponent
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.util.android.fadeIn
import tech.ketc.numeri.util.android.ui.enableRippleEffect
import tech.ketc.numeri.util.arch.coroutine.bindLaunch
import tech.ketc.numeri.util.coroutine.dispose

class TweetViewHolder(ctx: Context,
                      private val mClient: TwitterClient,
                      private val mOwner: LifecycleOwner,
                      imageLoadable: IImageLoadable,
                      private val mOnClick: (Tweet) -> Unit,
                      tweetUIComponent: ITweetUIComponent = TweetUIComponent())
    : RecyclerView.ViewHolder(tweetUIComponent.createView(ctx)),
        ITweetUIComponent by tweetUIComponent
        , IImageLoadable by imageLoadable {
    private val mContext = itemView.context
    private val mImageLoadTasks = ArrayList<Job>()

    init {
        itemView.enableRippleEffect()
    }

    fun bind(tweet: Tweet) {
        mImageLoadTasks.forEach(Job::dispose)
        mImageLoadTasks.clear()
        itemView.setOnClickListener { mOnClick(tweet) }
        val displayTweet = tweet.retweetedTweet ?: tweet
        setColor(tweet)
        setSubInfo(tweet)
        setDisplayTweet(displayTweet)
        setThumbs(displayTweet)
    }

    private fun ImageView.setImageUrl(url: String, cache: Boolean = true) {
        setImageBitmap(null)
        bindLaunch(mOwner) {
            val res = loadImage(url, cache).await()
            res.ifPresent { (b, _) -> setImageBitmap(b);fadeIn() }
        }.also { mImageLoadTasks.add(it) }
    }

    @SuppressLint("SetTextI18n")
    private fun setSubInfo(tweet: Tweet) {
        if (tweet.retweetedTweet != null) {
            subInfoText.visibility = View.VISIBLE
            subInfoText.text = "${tweet.user.screenName}さんからのRT"
            subInfoIcon.image = mContext.getDrawable(R.drawable.ic_autorenew_white_24dp)
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
        iconImage.setImageUrl(displayTweet.user.iconUrl)
        //iconImage.setOnClickListener { UserInfoActivity.start(ctx, mClient.id, displayTweet.user.id) }
        contentText.text = displayTweet.text
        sourceText.text = displayTweet.source
    }

    private fun setThumbs(displayTweet: Tweet) {
        val thumbs = displayTweet.mediaEntities.map { it.url + ":thumb" }
        (0..thumbnails.lastIndex).map { thumbs.getOrElse(it) { "" } }.forEachIndexed { i, url ->
            if (url.isNotEmpty()) {
                thumbnails[i].visibility = View.VISIBLE
                val imageView = thumbnails[i]
                imageView.setImageUrl(url, false)
                //imageView.setOnClickListener { MediaActivity.start(ctx, displayTweet.mediaEntities, i) }
            } else {
                thumbnails[i].setImageDrawable(null)
                thumbnails[i].visibility = View.GONE
            }
        }
    }

    private fun setColor(tweet: Tweet) {
        when {
            tweet.isMention(mClient) -> overlayRelative.backgroundColor = Color.parseColor("#60f46e42")
            tweet.retweetedTweet != null -> overlayRelative.backgroundColor = Color.parseColor("#6042dff4")
            else -> overlayRelative.backgroundColor = Color.parseColor("#00000000")
        }
    }
}