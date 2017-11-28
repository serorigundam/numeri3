package tech.ketc.numeri.ui.model

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.domain.repository.ITweetRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.client.showTweet
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.ui.model.delegate.ITweetOperator
import tech.ketc.numeri.ui.model.delegate.ImageLoadable
import tech.ketc.numeri.ui.model.delegate.TweetOperator
import tech.ketc.numeri.util.arch.StreamSource
import tech.ketc.numeri.util.arch.coroutine.asyncResponse
import tech.ketc.numeri.util.arch.coroutine.bindLaunch
import tech.ketc.numeri.util.arch.response.nullable
import javax.inject.Inject

class ConversationViewModel @Inject constructor(imageRepository: IImageRepository,
                                                private val mTweetRepository: ITweetRepository)
    : ViewModel(),
        IImageLoadable by ImageLoadable(imageRepository),
        ITweetOperator by TweetOperator(mTweetRepository) {

    private val mSource = StreamSource<Tweet>()
    val stream = mSource.stream()
    val reserveTweets = ArrayList<Tweet>()
    fun traceStart(owner: LifecycleOwner, client: TwitterClient, tweet: Tweet) {
        bindLaunch(owner) {
            var id = tweet.inReplyToStatusId
            while (id != -1L) {
                val t = asyncResponse {
                    client.showTweet(mTweetRepository, id)
                }.await().nullable() ?: return@bindLaunch
                id = t.inReplyToStatusId
                mSource.post(t)
            }
        }
    }
}