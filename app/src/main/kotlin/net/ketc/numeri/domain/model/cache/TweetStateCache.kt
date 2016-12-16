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

    fun setState(twitterClient: TwitterClient, tweet: Tweet, initializedState: State) {
        val stateMap = orPut(twitterClient.id)
        val state = stateMap[tweet.id]
        if (state != null)
            throw IllegalStateException()
        stateMap.put(tweet.id, initializedState)
    }

    fun changeState(twitterClient: TwitterClient, tweet: Tweet, state: State) {
        val stateMap = map[tweet.id] ?: throw IllegalStateException()
        stateMap[tweet.id] ?: throw IllegalStateException()
        stateMap.put(tweet.id, state)
    }

    fun getState(twitterClient: TwitterClient, tweet: Tweet): State {
        val stateMap = map[tweet.id] ?: throw IllegalStateException()
        return (stateMap[tweet.id] ?: throw IllegalStateException())
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