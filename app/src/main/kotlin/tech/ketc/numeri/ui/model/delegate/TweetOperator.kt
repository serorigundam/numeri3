package tech.ketc.numeri.ui.model.delegate

import tech.ketc.numeri.domain.repository.ITweetRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.TweetState
import tech.ketc.numeri.util.arch.coroutine.asyncResponse

class TweetOperator(private val mTweetRepository: ITweetRepository) : ITweetOperator {
    override fun favorite(client: TwitterClient, tweet: Tweet) = asyncResponse {
        val status = client.twitter.createFavorite(tweet.id)
        mTweetRepository.updateState(client, tweet.id, status.isFavorited, status.isRetweeted)
    }

    override fun unfavorite(client: TwitterClient, tweet: Tweet) = asyncResponse {
        val status = client.twitter.destroyFavorite(tweet.id)
        mTweetRepository.updateState(client, tweet.id, status.isFavorited, status.isRetweeted)
    }

    override fun retweet(client: TwitterClient, tweet: Tweet) = asyncResponse {
        val state = mTweetRepository.getState(client, tweet)
        client.twitter.retweetStatus(tweet.id)
        mTweetRepository.updateState(client, tweet.id, state.isFavorited, true)
    }

    override fun unretweet(client: TwitterClient, tweet: Tweet) = asyncResponse {
        val destroyId = mTweetRepository.getRetweetedId(client, tweet) ?: throw IllegalStateException()
        val state = mTweetRepository.getState(client, tweet)
        client.twitter.destroyStatus(destroyId)
        mTweetRepository.updateState(client, tweet.id, state.isFavorited, false)
    }

    override fun delete(client: TwitterClient, tweet: Tweet) = asyncResponse {
        if (client.id != tweet.user.id) throw IllegalArgumentException()
        client.twitter.destroyStatus(tweet.id)
        mTweetRepository.delete(tweet)
    }

    override fun getState(client: TwitterClient, tweet: Tweet): TweetState {
        return mTweetRepository.getState(client, tweet)
    }
}