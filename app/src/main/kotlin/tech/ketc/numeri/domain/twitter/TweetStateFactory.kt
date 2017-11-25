package tech.ketc.numeri.domain.twitter

import android.util.ArrayMap
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.TweetState
import twitter4j.Status

class TweetStateFactory : ITweetStateFactory {
    private val clientToStateMap = ArrayMap<TwitterClient, HashMap<Long, TweetState>>()


    override fun getOrPutState(client: TwitterClient, status: Status): TweetState {
        return clientToStateMap.getOrPut(client) { HashMap() }
                .getOrPut(status.id) { TweetState(status.isFavorited, status.isRetweeted) }
    }


    override fun updateState(client: TwitterClient, id: Long, isFav: Boolean, isRt: Boolean): TweetState {
        val map = (clientToStateMap[client] ?: throw IllegalStateException())
        map[id] ?: throw IllegalStateException()
        val state = TweetState(isFav, isRt)
        map.put(id, state)
        return state
    }

    override fun get(client: TwitterClient, tweet: Tweet): TweetState {
        val favMap = (clientToStateMap[client] ?: throw IllegalStateException())
        val id = tweet.id
        return favMap[id] ?: throw IllegalStateException()
    }

    override fun get(client: TwitterClient, status: Status): TweetState? {
        return clientToStateMap.getOrPut(client) { HashMap() }[status.id]
    }
}