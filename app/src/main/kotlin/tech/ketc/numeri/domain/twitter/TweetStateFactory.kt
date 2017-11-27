package tech.ketc.numeri.domain.twitter

import android.util.ArrayMap
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.TweetState
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import twitter4j.Status

class TweetStateFactory : ITweetStateFactory {
    private val clientToStateMap = ArrayMap<TwitterClient, HashMap<Long, TweetState>>()
    private val retweetIdMap = ArrayMap<TwitterClient, HashMap<Long, Long>>()

    private fun stateMap(client: TwitterClient) = clientToStateMap.getOrPut(client) {
        Logger.v(logTag, "crate new stateMap")
        HashMap()
    }

    private fun rtIdMap(client: TwitterClient) = retweetIdMap.getOrPut(client) {
        Logger.v(logTag, "crate new rtIdMap")
        HashMap()
    }


    override fun getOrPutState(client: TwitterClient, status: Status): TweetState {
        if (status.user.id == client.id && status.isRetweet) {
            rtIdMap(client).put(status.retweetedStatus.id, status.id)
        }
        status.currentUserRetweetId.takeIf { it != -1L }?.let {
            rtIdMap(client).put(it, status.id)
            Logger.v(logTag, "getOrPutState currentUserRetweetId")
        }
        return stateMap(client).getOrPut(status.id) { TweetState(status.isFavorited, status.isRetweeted) }
    }


    override fun updateState(client: TwitterClient, id: Long, isFav: Boolean, isRt: Boolean): TweetState {
        val map = (clientToStateMap[client] ?: throw IllegalStateException())
        map[id] ?: throw IllegalStateException()
        val state = TweetState(isFav, isRt)
        map.put(id, state)
        return state
    }

    override fun get(client: TwitterClient, tweet: Tweet): TweetState {
        val stateMap = (clientToStateMap[client] ?: throw IllegalStateException())
        val id = tweet.id
        return stateMap[id] ?: throw IllegalStateException()
    }

    override fun get(client: TwitterClient, status: Status): TweetState? {
        return stateMap(client)[status.id]
    }

    override fun getRetweetedId(client: TwitterClient, tweet: Tweet): Long? {
        return rtIdMap(client)[tweet.id]
    }
}