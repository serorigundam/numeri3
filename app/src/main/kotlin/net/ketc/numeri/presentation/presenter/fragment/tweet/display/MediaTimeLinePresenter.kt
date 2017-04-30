package net.ketc.numeri.presentation.presenter.fragment.tweet.display

import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.cache.showUser
import net.ketc.numeri.domain.model.cache.updateAndCache
import net.ketc.numeri.presentation.view.fragment.TimeLineFragmentInterface
import twitter4j.Paging
import twitter4j.Query

class MediaTimeLinePresenter(timeLineFragment: TimeLineFragmentInterface) : TimeLinePresenter(timeLineFragment) {

    override fun getTweets(paging: Paging): List<Tweet> {
        val user = client.showUser(fragment.display.foreignId)
        val queryStr = "filter:images filter:media exclude:retweets from:${user.screenName}"
        return client.twitter.search(Query(queryStr).apply {
            this.count = GET_TWEETS_COUNT
            this.resultType = Query.ResultType.recent
            this.maxId = paging.maxId
            this.sinceId = paging.sinceId
        }).tweets.map { it.updateAndCache(client) }
    }

    override fun beforeInitializeLoad() {
    }

    override fun afterInitializeLoad() {
        fragment.isRefreshable = true
    }

    companion object {
        val GET_TWEETS_COUNT = 20
    }
}
