package net.ketc.numeri.util.twitter

import io.reactivex.Observable
import net.ketc.numeri.domain.service.TwitterClient
import twitter4j.StatusUpdate
import java.io.File
import java.sql.ParameterMetaData
import kotlin.coroutines.experimental.buildSequence

class TweetSender(private val client: TwitterClient) {
    var text = ""
    var inReplyToStatusId: Long? = null
    var mediaList: List<File>? = null
    var isPossiblySensitive = false
    private var onProgressListener: (Int) -> Unit = {}

    val progressObservable: Observable<Int> by lazy {
        Observable.create<Int> { emitter ->
            onProgressListener = { progress ->
                if (progress == PROGRESS_MAX) {
                    emitter.onComplete()
                } else {
                    emitter.onNext(progress)
                }
            }
        }
    }

    fun send() {
        (inReplyToStatusId ?: 0).takeIf { it >= 0 } ?: throw IllegalStateException("InReplyStatusId must be a positive number")
        (mediaList?.size ?: 0).takeIf { it <= 4 } ?: throw IllegalStateException("The size of mediaList must be 4 or less")
        onProgressListener(0)
        val statusUpdate = StatusUpdate(text).apply {
            this@TweetSender.inReplyToStatusId?.let {
                inReplyToStatusId = it
            }
            mediaList?.takeIf { it.isNotEmpty() }?.let {
                setMediaIds(*uploadMedia(it).toList().toLongArray())
            }
        }
        client.twitter.updateStatus(statusUpdate)
        onProgressListener(PROGRESS_MAX)
    }

    private fun uploadMedia(mediaList: List<File>) = buildSequence {
        val diff = 80 / mediaList.size
        var progress = 0
        mediaList.forEach {
            yield(client.twitter.uploadMedia(it).mediaId)
            progress += diff
            onProgressListener(progress)
        }
    }

    companion object {
        private val PROGRESS_MAX = 100
    }
}

fun TwitterClient.createTweetSender(text: String = "",
                                    inReplyToStatusId: Long? = null,
                                    mediaList: List<File>? = null,
                                    isPossiblySensitive: Boolean = false): TweetSender {
    return TweetSender(this).apply {
        this.text = text
        this.inReplyToStatusId = inReplyToStatusId
        this.mediaList = mediaList
        this.isPossiblySensitive = isPossiblySensitive
    }
}