package tech.ketc.numeri.ui.view.recycler.timeline

import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
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
import tech.ketc.numeri.domain.twitter.model.*
import tech.ketc.numeri.ui.activity.media.MediaActivity
import tech.ketc.numeri.ui.activity.user.UserInfoActivity
import tech.ketc.numeri.ui.components.ITweetUIComponent
import tech.ketc.numeri.ui.components.TweetUIComponent
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.android.fadeIn
import tech.ketc.numeri.util.android.pref
import tech.ketc.numeri.util.android.ui.enableRippleEffect
import tech.ketc.numeri.util.arch.coroutine.bindLaunch
import tech.ketc.numeri.util.coroutine.dispose
import tech.ketc.numeri.util.logTag
import java.lang.ref.WeakReference

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
    private var mUseOrigIcon = mContext.pref.getBoolean(mContext.getString(R.string.pref_key_use_orig_icon), false)

    init {
        itemView.enableRippleEffect()
        val observer = createLifecycleObserver()
        mOwner.lifecycle.addObserver(observer)
        observer.vhDestroyed = {
            mOwner.lifecycle.removeObserver(observer)
        }
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

    private fun ImageView.setImageUrl(url: String, cache: Boolean = true, finish: () -> Unit = {}) {
        setImageBitmap(null)
        bindLaunch(mOwner) {
            val res = loadImage(url, cache).await()
            res.ifPresent { (b, _) -> setImageBitmap(b);fadeIn() }
            finish()
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
        iconBack.backgroundColor = mContext.getColor(R.color.image_background_transparency)
        iconImage.setImageUrl(displayTweet.user.getIconUrl(mUseOrigIcon)) {
            iconBack.backgroundColor = mContext.getColor(R.color.transparent)
        }
        iconImage.setOnClickListener { UserInfoActivity.start(mContext, mClient, displayTweet.user) }
        contentText.text = displayTweet.text
        sourceText.text = displayTweet.source
        if (displayTweet.user.isProtected) {
            protectedIndicator.visibility = View.VISIBLE
        } else {
            protectedIndicator.visibility = View.GONE
        }
    }

    private fun setThumbs(displayTweet: Tweet) {
        val entities = displayTweet.mediaEntities
        val thumbs = entities.map { it.url + ":thumb" }
        (0..thumbnails.lastIndex).map { thumbs.getOrElse(it) { "" } }.forEachIndexed { i, url ->
            if (url.isNotEmpty()) {
                thumbnails[i].visibility = View.VISIBLE
                val imageView = thumbnails[i]
                imageView.setImageUrl(url, false)
                imageView.setOnClickListener {
                    val entity = entities[i]
                    if (entity.type == MediaType.ANIMATED_GIF || entity.type == MediaType.VIDEO) {
                        val variant = entity.variants.maxBy { it.bitrate }!!
                        mContext.startActivity(variant.toIntent())
                    } else {
                        MediaActivity.start(mContext, entities, displayTweet.user.screenName, i)
                    }
                }
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

    private fun createLifecycleObserver() = object : LifecycleObserver {
        val ref = WeakReference<TweetViewHolder>(this@TweetViewHolder)
        var vhDestroyed = {}
        private fun dispose() {
            Logger.v(logTag, "dispose")
            vhDestroyed()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            ref.get()?.let { holder ->
                holder.mUseOrigIcon = mContext.pref.getBoolean(mContext.getString(R.string.pref_key_use_orig_icon), false)
            } ?: dispose()
        }
    }
}