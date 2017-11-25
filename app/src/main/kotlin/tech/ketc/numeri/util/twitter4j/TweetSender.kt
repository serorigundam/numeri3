package tech.ketc.numeri.util.twitter4j

import tech.ketc.numeri.domain.twitter.client.TwitterClient
import twitter4j.StatusUpdate
import java.io.File
import kotlin.coroutines.experimental.buildSequence

class TweetSender(private val client: TwitterClient) {
    var text = ""
    var inReplyToStatusId: Long? = null
    var mediaList: List<File>? = null
    var isPossiblySensitive = false
    fun send() {
        (inReplyToStatusId ?: 0).takeIf { it >= 0 } ?: throw IllegalStateException("InReplyStatusId must be a positive number")
        (mediaList?.size ?: 0).takeIf { it <= 4 } ?: throw IllegalStateException("The size of mediaList must be 4 or less")
        val statusUpdate = StatusUpdate(text).apply {
            this@TweetSender.inReplyToStatusId?.let {
                inReplyToStatusId = it
            }
            mediaList?.takeIf { it.isNotEmpty() }?.let {
                setMediaIds(*uploadMedia(it).toList().toLongArray())
            }
        }
        client.twitter.updateStatus(statusUpdate)
    }

    private fun uploadMedia(mediaList: List<File>) = buildSequence {
        mediaList.forEach {
            yield(client.twitter.uploadMedia(it).mediaId)
        }
    }
}

fun TwitterClient.sendTweet(text: String = "",
                            inReplyToStatusId: Long? = null,
                            mediaList: List<File>? = null,
                            isPossiblySensitive: Boolean = false) {
    TweetSender(this).run {
        this.text = text
        this.inReplyToStatusId = inReplyToStatusId
        this.mediaList = mediaList
        this.isPossiblySensitive = isPossiblySensitive
        send()
    }
}