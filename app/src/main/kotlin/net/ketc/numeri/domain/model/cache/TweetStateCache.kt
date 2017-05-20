package net.ketc.numeri.domain.model.cache

import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.service.TwitterClient
import java.io.Serializable
import kotlin.collections.LinkedHashMap

abstract class TweetStateCache<State : Serializable> {
    private val map = LinkedHashMap<Long, LinkedHashMap<Long, State>>()

    fun getOrPut(twitterClient: TwitterClient) = map.getOrPut(twitterClient.id) { LinkedHashMap() }


    fun changeState(twitterClient: TwitterClient, tweet: Tweet, state: State) {
        val stateMap = getOrPut(twitterClient)
        stateMap.put(tweet.id, state)
    }

    fun getState(twitterClient: TwitterClient, tweet: Tweet): State {
        return getStateOrNull(twitterClient, tweet)
                ?: throw IllegalStateException()
    }

    fun getStateOrNull(twitterClient: TwitterClient, tweet: Tweet) = getOrPut(twitterClient)[tweet.id]

    fun deleteState(twitterClient: TwitterClient) {
        map.remove(twitterClient.id)
    }

    fun deleteState(tweet: Tweet) {
        map.values.forEach { it.remove(tweet.id) }
    }
}

object FavoriteStateCache : TweetStateCache<Boolean>()
object RetweetStateCache : TweetStateCache<Boolean>()

fun TwitterClient.checkFavorite(tweet: Tweet) = FavoriteStateCache.getState(this, tweet)
fun TwitterClient.checkFavoriteOrElse(tweet: Tweet, elseAction: () -> Boolean) = FavoriteStateCache.getStateOrNull(this, tweet) ?: elseAction()
fun TwitterClient.favorite(tweet: Tweet) = FavoriteStateCache.changeState(this, tweet, true)
fun TwitterClient.unFavorite(tweet: Tweet) = FavoriteStateCache.changeState(this, tweet, false)

fun TwitterClient.checkRetweeted(tweet: Tweet) = RetweetStateCache.getState(this, tweet)
fun TwitterClient.checkRetwwtedOrElse(tweet: Tweet, elseAction: () -> Boolean) = RetweetStateCache.getStateOrNull(this, tweet) ?: elseAction()
fun TwitterClient.retweet(tweet: Tweet) = RetweetStateCache.changeState(this, tweet, true)
fun TwitterClient.unRetweet(tweet: Tweet) = RetweetStateCache.changeState(this, tweet, false)