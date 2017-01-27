package net.ketc.numeri.presentation.presenter.fragment.tweet.display

import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.cache.TweetFactory
import net.ketc.numeri.domain.model.cache.convertAndCacheOrGet
import net.ketc.numeri.presentation.view.fragment.TimeLineFragmentInterface
import twitter4j.Paging

class PublicTimeLinePresenter(timeLineFragment: TimeLineFragmentInterface) : TimeLinePresenter(timeLineFragment) {

    val targetUserId = fragment.display.foreignId

    override fun getTweets(paging: Paging): List<Tweet> = client.twitter.getUserTimeline(targetUserId, paging).map { it.convertAndCacheOrGet(client) }

    override fun beforeInitializeLoad() {
    }

    override fun afterInitializeLoad() {
    }

}
