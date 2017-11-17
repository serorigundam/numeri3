package tech.ketc.numeri.domain.twitter.client

import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.util.arch.BindingLifecycleStream
import twitter4j.StatusDeletionNotice

interface TwitterStream {
    val latestTweet: BindingLifecycleStream<Tweet>
    val latestTweetDeletionNotice: BindingLifecycleStream<StatusDeletionNotice>
    val latestFavoriteNotice: BindingLifecycleStream<StatusNotice>
    val latestUnFavoriteNotice: BindingLifecycleStream<StatusNotice>
    val latestFollowNotice: BindingLifecycleStream<UserNotice>
    val latestUnFollowNotice: BindingLifecycleStream<UserNotice>
    val latestBlockNotice: BindingLifecycleStream<UserNotice>
    val latestUnBlockNotice: BindingLifecycleStream<UserNotice>
}