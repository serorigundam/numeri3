package tech.ketc.numeri.domain.twitter

import android.util.ArrayMap
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.TweetState
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import twitter4j.Status
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TweetStateFactory : ITweetStateFactory {
    private val clientToStateMap = ArrayMap<TwitterClient, HashMap<Long, TweetState>>()
    private val retweetIdMap = ArrayMap<TwitterClient, HashMap<Long, Long>>()
    private val stateLock = HashMap<Long, ReentrantLock>()
    private fun stateLock(id: Long) = stateLock.getOrPut(id) { ReentrantLock() }
    private fun stateUnLock(id: Long) {
        val holdCount = stateLock(id).holdCount
        if (holdCount == 1) stateLock.remove(id)
    }

    private fun stateMap(client: TwitterClient) = clientToStateMap.getOrPut(client) {
        Logger.v(logTag, "crate new stateMap")
        HashMap()
    }

    private fun rtIdMap(client: TwitterClient) = retweetIdMap.getOrPut(client) {
        Logger.v(logTag, "crate new rtIdMap")
        HashMap()
    }


    override fun getOrPutState(client: TwitterClient, status: Status): TweetState {
        val statusId = status.id
        if (status.user.id == client.id && status.isRetweet) {
            rtIdMap(client).put(status.retweetedStatus.id, statusId)
        }
        status.currentUserRetweetId.takeIf { it != -1L }?.let {
            rtIdMap(client).put(it, status.id)
            Logger.v(logTag, "getOrPutState currentUserRetweetId")
        }
        fun put(): TweetState {
            val state = stateMap(client).getOrPut(statusId) { TweetState(status.isFavorited, status.isRetweeted) }
            stateUnLock(statusId)
            return state
        }
        return stateLock(status.id).withLock(::put)
    }


    override fun updateState(client: TwitterClient, id: Long, isFav: Boolean?, isRt: Boolean?): TweetState {
        val map = (clientToStateMap[client] ?: throw IllegalStateException())
        val s = map[id] ?: throw IllegalStateException()
        fun update(): TweetState {
            val state = TweetState(isFav ?: s.isFavorited, isRt ?: s.isRetweeted)
            map.put(id, state)
            stateUnLock(id)
            return state
        }
        return stateLock(id).withLock(::update)
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