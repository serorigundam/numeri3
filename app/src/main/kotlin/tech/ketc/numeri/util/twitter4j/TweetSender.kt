package tech.ketc.numeri.util.twitter4j

import android.content.Context
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.util.android.ImageResizer
import twitter4j.StatusUpdate
import java.io.File
import kotlin.coroutines.experimental.buildSequence

class TweetSender(private val mClient: TwitterClient,
                  text: String = "",
                  inReplyToStatusId: Long? = null,
                  mediaList: List<File>? = null,
                  isPossiblySensitive: Boolean = false) {
    private var mText = text
    private var mInReplyToStatusId: Long? = inReplyToStatusId
    private var mMediaList: List<File>? = mediaList
    private var mIsPossiblySensitive = isPossiblySensitive
    var onProgress: (Int) -> Unit = {}

    companion object {
        private val MEDIA_SIZE_MAX = 5242880
    }

    fun send(ctx: Context) {
        (mInReplyToStatusId ?: 0).takeIf { it >= 0 } ?: throw IllegalStateException("InReplyStatusId must be a positive number")
        (mMediaList?.size ?: 0).takeIf { it <= 4 } ?: throw IllegalStateException("The size of mediaList must be 4 or less")
        onProgress(0)
        val statusUpdate = StatusUpdate(mText).apply {
            mInReplyToStatusId?.let { inReplyToStatusId = it }
            mMediaList?.takeIf { it.isNotEmpty() }?.let {
                setMediaIds(*uploadMedia(it, ctx).toList().toLongArray())
            }
            isPossiblySensitive = mIsPossiblySensitive
        }
        mClient.twitter.updateStatus(statusUpdate)
        onProgress(100)
    }

    private fun uploadMedia(mediaList: List<File>, ctx: Context) = buildSequence {
        val diff = 80 / mediaList.size
        var progress = 0
        mediaList.forEach {
            val imageResizer = ImageResizer(ctx, it, MEDIA_SIZE_MAX)
            val mediaFile = if (imageResizer.resize()) {
                imageResizer.result
            } else it
            yield(mClient.twitter.uploadMedia(mediaFile).mediaId)
            progress += diff
            onProgress(progress)
        }
    }
}

fun TwitterClient.createSender(text: String = "",
                               inReplyToStatusId: Long? = null,
                               mediaList: List<File>? = null,
                               isPossiblySensitive: Boolean = false)
        = TweetSender(this, text, inReplyToStatusId, mediaList, isPossiblySensitive)