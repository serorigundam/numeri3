package net.ketc.numeri.presentation.presenter.fragment.tweet.display

import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.cache.convertAndCacheOrGet
import net.ketc.numeri.domain.model.isMention
import net.ketc.numeri.presentation.view.fragment.TimeLineFragmentInterface
import twitter4j.Paging

class MentionsPresenter(timeLineFragment: TimeLineFragmentInterface) : TimeLinePresenter(timeLineFragment) {

    override fun getTweets(paging: Paging): List<Tweet> = client.twitter.getMentionsTimeline(paging).map { it.convertAndCacheOrGet(client) }


    override fun beforeInitializeLoad() {
    }

    override fun afterInitializeLoad() {
        client.stream.onStatusFlowable
                .filter { it.isMention(client) }
                .subscribe {
                    fragment.insert(it)
                }
                .autoDispose()
    }
}