package net.ketc.numeri.domain.model.cache

import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.service.TwitterClient
import java.io.Serializable
import java.util.*

abstract class TweetStateCache<State : Serializable> {
    private val map = LinkedHashMap<Long, LinkedHashMap<Long, State>>()

    private fun orPut(clientId: Long): LinkedHashMap<Long, State> {
        val stateMap = map[clientId]
        if (stateMap == null) {
            val newStateMap = LinkedHashMap<Long, State>()
            map.put(clientId, newStateMap)
            return newStateMap
        } else {
            return stateMap
        }
    }

    fun changeState(twitterClient: TwitterClient, tweet: Tweet, state: State) {
        val stateMap = map[twitterClient.id] ?: orPut(twitterClient.id)
        stateMap.put(tweet.id, state)
    }

    fun getState(twitterClient: TwitterClient, tweet: Tweet): State {
        return getStateOrNull(twitterClient, tweet) ?: throw IllegalStateException()
    }

    fun getStateOrNull(twitterClient: TwitterClient, tweet: Tweet): State? {
        val stateMap = map[twitterClient.id] ?: throw IllegalStateException()
        return stateMap[tweet.id]
    }

    fun deleteState(twitterClient: TwitterClient) {
        map.remove(twitterClient.id)
    }

    fun deleteState(tweet: Tweet) {
        map.values.forEach { it.remove(tweet.id) }
    }
}

object FavoriteStateCache : TweetStateCache<Boolean>()
object RetweetStateCache : TweetStateCache<Boolean>()

fun TwitterClient.isFavorite(tweet: Tweet) = FavoriteStateCache.getState(this, tweet)
fun TwitterClient.setFavorite(tweet: Tweet) = FavoriteStateCache.changeState(this, tweet, true)
fun TwitterClient.setUnFavorite(tweet: Tweet) = FavoriteStateCache.changeState(this, tweet, false)

fun TwitterClient.isRetweeted(tweet: Tweet) = RetweetStateCache.getState(this, tweet)
fun TwitterClient.setRetweet(tweet: Tweet) = RetweetStateCache.changeState(this, tweet, true)
fun TwitterClient.setUnRetweet(tweet: Tweet) = RetweetStateCache.changeState(this, tweet, false)